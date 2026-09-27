package com.sentrygate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class IssueDto {
    private Long id;
    private Long githubIssueId;
    private Integer number;
    private String title;
    private String body;
    private String authorLogin;
    private String state;
    private List<String> labels;
    private String htmlUrl;
    private Long repositoryId;
    private String repositoryFullName;
    private Boolean isPullRequest;
    private String duplicateStatus;
    private List<IssueSimilarityDto> similarities;
    private OffsetDateTime githubCreatedAt;
    private OffsetDateTime githubUpdatedAt;
    private OffsetDateTime createdAt;
}
