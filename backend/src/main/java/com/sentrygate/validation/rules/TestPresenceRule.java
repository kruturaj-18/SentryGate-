package com.sentrygate.validation.rules;

import com.sentrygate.validation.RuleResult;
import com.sentrygate.validation.ValidationContext;
import com.sentrygate.validation.ValidationRule;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Checks if any test files are present in the PR changeset.
 */
@Component
public class TestPresenceRule implements ValidationRule {

    private static final List<String> DEFAULT_TEST_PATTERNS = List.of(
            ".*\\.test\\..*", ".*\\.spec\\..*", ".*Test\\.java", ".*Tests\\.java",
            ".*test.*\\.py", ".*_test\\.go", ".*\\.test\\.ts", ".*\\.spec\\.ts"
    );

    @Override
    public String getRuleCode() { return "TEST_PRESENCE"; }

    @Override
    public String getRuleName() { return "Test Presence Check"; }

    @Override
    public String getDefaultSeverity() { return "MEDIUM"; }

    @Override
    public RuleResult validate(ValidationContext context) {
        Map<String, Object> config = context.ruleConfig();
        boolean requireTests = config == null || !config.containsKey("requireTests")
                || Boolean.parseBoolean(String.valueOf(config.get("requireTests")));

        if (!requireTests) {
            return RuleResult.skipped(getRuleCode(), getRuleName(), "Test requirement is disabled.");
        }

        @SuppressWarnings("unchecked")
        List<String> testPatterns = config != null && config.containsKey("testPatterns")
                ? (List<String>) config.get("testPatterns")
                : DEFAULT_TEST_PATTERNS;

        List<String> changedFiles = context.changedFileNames();
        boolean hasTests = changedFiles.stream().anyMatch(file ->
                testPatterns.stream().anyMatch(pattern -> {
                    // Convert glob-like patterns to regex
                    String regex = pattern.replace(".", "\\.").replace("*", ".*");
                    return file.matches(regex) || file.contains("test") || file.contains("spec") || file.contains("Test");
                })
        );

        if (hasTests) {
            return RuleResult.pass(getRuleCode(), getRuleName(), getDefaultSeverity(),
                    "Test files detected in the changeset.");
        } else {
            return RuleResult.fail(getRuleCode(), getRuleName(), getDefaultSeverity(),
                    "No test files detected. Please include tests with your changes.");
        }
    }
}
