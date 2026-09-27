package com.sentrygate.validation.rules;

import com.sentrygate.validation.RuleResult;
import com.sentrygate.validation.ValidationContext;
import com.sentrygate.validation.ValidationRule;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * Validates PR description length meets minimum requirement.
 */
@Component
public class PrDescriptionLengthRule implements ValidationRule {

    private static final int DEFAULT_MIN_LENGTH = 50;

    @Override
    public String getRuleCode() { return "PR_DESCRIPTION_LENGTH"; }

    @Override
    public String getRuleName() { return "PR Description Length"; }

    @Override
    public String getDefaultSeverity() { return "LOW"; }

    @Override
    public RuleResult validate(ValidationContext context) {
        String body = context.prBody();
        Map<String, Object> config = context.ruleConfig();
        int minLength = config != null && config.containsKey("minLength")
                ? ((Number) config.get("minLength")).intValue()
                : DEFAULT_MIN_LENGTH;

        if (!StringUtils.hasText(body)) {
            return RuleResult.fail(getRuleCode(), getRuleName(), getDefaultSeverity(),
                    "PR description is empty. Minimum length is " + minLength + " characters.");
        }

        int actualLength = body.trim().length();
        if (actualLength >= minLength) {
            return RuleResult.pass(getRuleCode(), getRuleName(), getDefaultSeverity(),
                    "Description has " + actualLength + " characters (minimum: " + minLength + ").");
        } else {
            return RuleResult.fail(getRuleCode(), getRuleName(), getDefaultSeverity(),
                    "Description is too short: " + actualLength + " characters. Minimum required: " + minLength + ".");
        }
    }
}
