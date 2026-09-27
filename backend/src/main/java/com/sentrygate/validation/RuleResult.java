package com.sentrygate.validation;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Result of a single validation rule execution.
 */
@Getter
@Builder
public class RuleResult {
    private final String ruleCode;
    private final String ruleName;
    private final String status;      // PASS, FAIL, WARNING, SKIPPED
    private final String severity;    // INFO, LOW, MEDIUM, HIGH, CRITICAL
    private final String message;
    private final Map<String, Object> details;

    @Builder.Default
    private final OffsetDateTime timestamp = OffsetDateTime.now();

    // Convenience factory methods
    public static RuleResult pass(String ruleCode, String ruleName, String severity, String message) {
        return RuleResult.builder()
                .ruleCode(ruleCode).ruleName(ruleName)
                .status("PASS").severity(severity).message(message)
                .build();
    }

    public static RuleResult fail(String ruleCode, String ruleName, String severity, String message) {
        return RuleResult.builder()
                .ruleCode(ruleCode).ruleName(ruleName)
                .status("FAIL").severity(severity).message(message)
                .build();
    }

    public static RuleResult warning(String ruleCode, String ruleName, String severity, String message) {
        return RuleResult.builder()
                .ruleCode(ruleCode).ruleName(ruleName)
                .status("WARNING").severity(severity).message(message)
                .build();
    }

    public static RuleResult skipped(String ruleCode, String ruleName, String message) {
        return RuleResult.builder()
                .ruleCode(ruleCode).ruleName(ruleName)
                .status("SKIPPED").severity("INFO").message(message)
                .build();
    }
}
