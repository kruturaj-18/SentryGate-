package com.sentrygate.validation.rules;

import com.sentrygate.validation.RuleResult;
import com.sentrygate.validation.ValidationContext;
import com.sentrygate.validation.ValidationRule;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Validates PR title format against configurable regex pattern.
 * Default enforces conventional commits format.
 */
@Component
public class PrTitleFormatRule implements ValidationRule {

    private static final String DEFAULT_PATTERN =
            "^(feat|fix|docs|refactor|test|chore|style|perf|ci|build)(\\(.*\\))?:\\s.+$";

    @Override
    public String getRuleCode() { return "PR_TITLE_FORMAT"; }

    @Override
    public String getRuleName() { return "PR Title Format"; }

    @Override
    public String getDefaultSeverity() { return "MEDIUM"; }

    @Override
    public RuleResult validate(ValidationContext context) {
        String title = context.prTitle();
        if (!StringUtils.hasText(title)) {
            return RuleResult.fail(getRuleCode(), getRuleName(), getDefaultSeverity(),
                    "PR title is empty.");
        }

        Map<String, Object> config = context.ruleConfig();
        String pattern = config != null && config.containsKey("pattern")
                ? (String) config.get("pattern")
                : DEFAULT_PATTERN;

        if (Pattern.matches(pattern, title)) {
            return RuleResult.pass(getRuleCode(), getRuleName(), getDefaultSeverity(),
                    "Title follows the required format.");
        } else {
            String message = config != null && config.containsKey("message")
                    ? (String) config.get("message")
                    : "Title does not follow conventional commits format. Expected: feat|fix|docs|refactor|...";
            return RuleResult.fail(getRuleCode(), getRuleName(), getDefaultSeverity(), message);
        }
    }
}
