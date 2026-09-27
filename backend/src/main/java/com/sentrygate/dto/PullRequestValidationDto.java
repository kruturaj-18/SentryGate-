package com.sentrygate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PullRequestValidationDto {
    private Long id;
    private Long pullRequestId;
    private Integer pullRequestNumber;
    private String pullRequestTitle;
    private String overallStatus;
    private Integer passCount;
    private Integer failCount;
    private Integer warningCount;
    private Integer skippedCount;
    private String triggeredBy;
    private OffsetDateTime startedAt;
    private OffsetDateTime completedAt;
    private OffsetDateTime createdAt;
    private List<ValidationResultDto> results;
}
