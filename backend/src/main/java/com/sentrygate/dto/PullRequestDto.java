package com.sentrygate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PullRequestDto {
    private Long id;
    private Long githubPrId;
    private Integer number;
    private String title;
    private String body;
    private String authorLogin;
    private String headBranch;
    private String baseBranch;
    private String state;
    private Boolean draft;
    private Boolean merged;
    private Integer additions;
    private Integer deletions;
    private Integer changedFiles;
    private String htmlUrl;
    private Long repositoryId;
    private String repositoryFullName;
    private String validationStatus;
    private OffsetDateTime githubCreatedAt;
    private OffsetDateTime githubUpdatedAt;
    private OffsetDateTime createdAt;
}
