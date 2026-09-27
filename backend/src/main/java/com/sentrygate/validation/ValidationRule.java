package com.sentrygate.validation;

/**
 * Interface for all validation rules in the SentryGate validation engine.
 * Each rule is independent, testable, and configurable.
 */
public interface ValidationRule {

    /** Unique machine-readable code for this rule */
    String getRuleCode();

    /** Human-readable name for display */
    String getRuleName();

    /** Default severity if not configured */
    String getDefaultSeverity();

    /**
     * Execute this rule against the given context.
     * @param context the PR data and rule configuration
     * @return a RuleResult indicating PASS, FAIL, WARNING, or SKIPPED
     */
    RuleResult validate(ValidationContext context);
}
