package com.sentrygate.validation.rules;

import com.sentrygate.validation.RuleResult;
import com.sentrygate.validation.ValidationContext;
import com.sentrygate.validation.ValidationRule;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Validates that required files are present in the PR changeset.
 *
 * <p>Issue #5 fix: the previous implementation always produced an empty
 * {@code missing} list because it treated "found in changedFileNames" as
 * proof of existence. When {@code changedFileNames} is empty (or the required
 * file was not touched by the PR), the list was vacuously empty and the rule
 * always passed.
 *
 * <p>Corrected semantics:
 * <ol>
 *   <li>If {@code changedFileNames} is empty we cannot draw any conclusion —
 *       skip the rule rather than silently passing it.</li>
 *   <li>If the PR <em>does</em> touch source / config files but the required
 *       file is absent from the changeset, that is a genuine failure.</li>
 *   <li>A configurable {@code requiredFiles} list (default: empty, so the
 *       rule is a no-op unless explicitly configured) keeps defaults safe.</li>
 * </ol>
 */
@Component
public class RequiredFilesRule implements ValidationRule {

    /**
     * Default required files list is intentionally empty so that no PR is
     * blocked by this rule unless an admin explicitly configures it for the
     * repository.
     */
    private static final List<String> DEFAULT_REQUIRED = List.of();

    @Override
    public String getRuleCode() { return "REQUIRED_FILES"; }

    @Override
    public String getRuleName() { return "Required Files Check"; }

    @Override
    public String getDefaultSeverity() { return "HIGH"; }

    @Override
    public RuleResult validate(ValidationContext context) {
        Map<String, Object> config = context.ruleConfig();

        @SuppressWarnings("unchecked")
        List<String> required = (config != null && config.containsKey("requiredFiles"))
                ? (List<String>) config.get("requiredFiles")
                : DEFAULT_REQUIRED;

        // Nothing configured — nothing to enforce
        if (required.isEmpty()) {
            return RuleResult.pass(getRuleCode(), getRuleName(), getDefaultSeverity(),
                    "No required files configured for this repository.");
        }

        List<String> changedFiles = context.changedFileNames();

        // Issue #5 fix: if changedFileNames is empty we have no data to evaluate.
        // Skipping is safer than vacuously passing (or wrongly failing).
        if (changedFiles.isEmpty()) {
            return RuleResult.skipped(getRuleCode(), getRuleName(),
                    "Changed file list is unavailable; skipping required-files check.");
        }

        // Issue #5 fix: build the missing list correctly — a required file is
        // considered MISSING from this PR if it does not appear in the actual
        // diff.  If the repo has been configured to require these files in every
        // PR that touches source code, this produces real failures.
        List<String> missing = new ArrayList<>();
        for (String req : required) {
            boolean found = changedFiles.stream()
                    .anyMatch(f -> f.equalsIgnoreCase(req) || f.endsWith("/" + req));
            if (!found) {
                missing.add(req);
            }
        }

        if (missing.isEmpty()) {
            return RuleResult.pass(getRuleCode(), getRuleName(), getDefaultSeverity(),
                    "All required files are present in the changeset: " + String.join(", ", required));
        } else {
            return RuleResult.fail(getRuleCode(), getRuleName(), getDefaultSeverity(),
                    "Required files missing from this PR's changeset: " + String.join(", ", missing)
                            + ". Please include or update these files.");
        }
    }
}
