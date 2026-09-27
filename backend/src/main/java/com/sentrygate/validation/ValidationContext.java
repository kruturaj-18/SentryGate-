package com.sentrygate.validation;

import java.util.Map;

/**
 * Context object passed to each validation rule.
 * Contains all PR data needed for validation decisions.
 */
public record ValidationContext(
    String prTitle,
    String prBody,
    String headBranch,
    String baseBranch,
    int additions,
    int deletions,
    int changedFiles,
    boolean isDraft,
    java.util.List<String> changedFileNames,
    Map<String, Object> ruleConfig
) {
    public int totalChangedLines() {
        return additions + deletions;
    }
}
