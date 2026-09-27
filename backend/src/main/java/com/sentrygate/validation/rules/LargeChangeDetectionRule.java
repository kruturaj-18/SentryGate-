package com.sentrygate.validation.rules;

import com.sentrygate.validation.RuleResult;
import com.sentrygate.validation.ValidationContext;
import com.sentrygate.validation.ValidationRule;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Detects PRs with too many changed lines.
 * Issues a WARNING (not FAIL) to encourage smaller PRs without blocking.
 */
@Component
public class LargeChangeDetectionRule implements ValidationRule {

    private static final int DEFAULT_MAX_ADDITIONS = 500;
    private static final int DEFAULT_MAX_DELETIONS = 300;

    @Override
    public String getRuleCode() { return "LARGE_CHANGE_DETECTION"; }

    @Override
    public String getRuleName() { return "Large Change Detection"; }

    @Override
    public String getDefaultSeverity() { return "MEDIUM"; }

    @Override
    public RuleResult validate(ValidationContext context) {
        Map<String, Object> config = context.ruleConfig();
        int maxAdditions = config != null && config.containsKey("maxAdditions")
                ? ((Number) config.get("maxAdditions")).intValue()
                : DEFAULT_MAX_ADDITIONS;
        int maxDeletions = config != null && config.containsKey("maxDeletions")
                ? ((Number) config.get("maxDeletions")).intValue()
                : DEFAULT_MAX_DELETIONS;

        int additions = context.additions();
        int deletions = context.deletions();
        boolean tooLarge = additions > maxAdditions || deletions > maxDeletions;

        if (!tooLarge) {
            return RuleResult.pass(getRuleCode(), getRuleName(), getDefaultSeverity(),
                    additions + " additions and " + deletions + " deletions are within limits (" +
                    maxAdditions + "/" + maxDeletions + ").");
        } else {
            String msg = "PR is large: " + additions + " additions, " + deletions + " deletions. " +
                    "Limit: " + maxAdditions + " additions / " + maxDeletions + " deletions. " +
                    "Consider splitting into smaller PRs.";
            return RuleResult.warning(getRuleCode(), getRuleName(), getDefaultSeverity(), msg);
        }
    }
}
