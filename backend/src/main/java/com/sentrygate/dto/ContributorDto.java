package com.sentrygate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ContributorDto {
    private Long id;
    private Long githubId;
    private String login;
    private String name;
    private String avatarUrl;
    private Long repositoryId;
    private String repositoryFullName;
    private Integer totalPrs;
    private Integer mergedPrs;
    private Integer rejectedPrs;
    private Integer openPrs;
    private Integer totalIssues;
    private Integer closedIssues;
    private Integer validationPasses;
    private Integer validationFailures;
    private Integer contributionCount;
    private OffsetDateTime firstContributionAt;
    private OffsetDateTime lastContributionAt;
    private TrustScoreDto trustScore;
    private OffsetDateTime createdAt;
}
