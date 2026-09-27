package com.sentrygate.validation.rules;

import com.sentrygate.validation.RuleResult;
import com.sentrygate.validation.ValidationContext;
import com.sentrygate.validation.ValidationRule;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Validates branch naming convention against allowed patterns.
 */
@Component
public class BranchNamingRule implements ValidationRule {

    private static final List<String> DEFAULT_PATTERNS = List.of(
            "feature/.*", "bugfix/.*", "hotfix/.*", "main", "develop", "release/.*"
    );

    @Override
    public String getRuleCode() { return "BRANCH_NAMING"; }

    @Override
    public String getRuleName() { return "Branch Naming Convention"; }

    @Override
    public String getDefaultSeverity() { return "LOW"; }

    @Override
    public RuleResult validate(ValidationContext context) {
        String branch = context.headBranch();
        if (!StringUtils.hasText(branch)) {
            return RuleResult.skipped(getRuleCode(), getRuleName(), "Branch name not available.");
        }

        Map<String, Object> config = context.ruleConfig();
        @SuppressWarnings("unchecked")
        List<String> allowedPatterns = config != null && config.containsKey("allowedPatterns")
                ? (List<String>) config.get("allowedPatterns")
                : DEFAULT_PATTERNS;

        boolean matches = allowedPatterns.stream()
                .anyMatch(branch::matches);

        if (matches) {
            return RuleResult.pass(getRuleCode(), getRuleName(), getDefaultSeverity(),
                    "Branch '" + branch + "' matches an allowed naming pattern.");
        } else {
            return RuleResult.fail(getRuleCode(), getRuleName(), getDefaultSeverity(),
                    "Branch '" + branch + "' does not match any allowed pattern. " +
                    "Allowed: " + String.join(", ", allowedPatterns));
        }
    }
}
