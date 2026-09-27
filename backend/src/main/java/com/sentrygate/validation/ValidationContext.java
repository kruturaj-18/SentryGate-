package com.sentrygate.validation;

import lombok.Builder;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Context object passed to each validation rule.
 * Contains all PR data needed for validation decisions.
 */
@Builder
public record ValidationContext(
    String prTitle,
    String prBody,
    String headBranch,
    String baseBranch,
    int additions,
    int deletions,
    int changedFiles,
    boolean isDraft,
    List<String> changedFileNames,
    Map<String, Object> ruleConfig
) {
    public ValidationContext {
        if (changedFileNames == null) {
            changedFileNames = Collections.emptyList();
        }
        if (ruleConfig == null) {
            ruleConfig = Collections.emptyMap();
        }
    }

    public int totalChangedLines() {
        return additions + deletions;
    }
}
