package com.sentrygate.validation.rules;

import com.sentrygate.validation.RuleResult;
import com.sentrygate.validation.ValidationContext;
import com.sentrygate.validation.ValidationRule;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic static analysis on file names and PR metadata.
 * Checks for debug statements in file names, TODO patterns, and suspicious file types.
 * NOTE: Does NOT parse actual file content (would require GitHub API integration).
 */
@Component
public class StaticAnalysisRule implements ValidationRule {

    private static final List<String> SUSPICIOUS_EXTENSIONS = List.of(
        ".class", ".jar", ".war", ".exe", ".dll", ".so", ".dylib"
    );

    private static final List<String> DEBUG_FILENAME_PATTERNS = List.of(
        "debug", "temp_", "_temp", "test123", "fixme", "hack_", "quick_fix"
    );

    @Override
    public String getRuleCode() { return "STATIC_ANALYSIS"; }

    @Override
    public String getRuleName() { return "Basic Static Analysis"; }

    @Override
    public String getDefaultSeverity() { return "LOW"; }

    @Override
    public RuleResult validate(ValidationContext context) {
        List<String> changedFiles = context.changedFileNames();
        List<String> warnings = new ArrayList<>();

        for (String file : changedFiles) {
            String lowerFile = file.toLowerCase();

            // Check for compiled/binary files that shouldn't be committed
            for (String ext : SUSPICIOUS_EXTENSIONS) {
                if (lowerFile.endsWith(ext)) {
                    warnings.add("Compiled/binary file detected: " + file);
                }
            }

            // Check for suspicious naming patterns
            for (String pattern : DEBUG_FILENAME_PATTERNS) {
                if (lowerFile.contains(pattern)) {
                    warnings.add("Suspicious filename pattern '" + pattern + "': " + file);
                }
            }
        }

        // Check if PR description contains TODO/FIXME
        String body = context.prBody();
        if (body != null && (body.toUpperCase().contains("TODO") || body.toUpperCase().contains("FIXME"))) {
            warnings.add("PR description contains TODO/FIXME. Consider resolving before merging.");
        }

        if (warnings.isEmpty()) {
            return RuleResult.pass(getRuleCode(), getRuleName(), getDefaultSeverity(),
                    "Basic static analysis passed. No obvious issues detected.");
        } else if (warnings.size() <= 2) {
            return RuleResult.warning(getRuleCode(), getRuleName(), getDefaultSeverity(),
                    "Static analysis found potential issues:\n" + String.join("\n", warnings));
        } else {
            return RuleResult.fail(getRuleCode(), getRuleName(), getDefaultSeverity(),
                    "Static analysis found multiple issues:\n" + String.join("\n", warnings));
        }
    }
}
