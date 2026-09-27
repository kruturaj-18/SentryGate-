package com.sentrygate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RepositoryDto {
    private Long id;
    private Long githubRepoId;
    private String name;
    private String fullName;
    private String owner;
    private String htmlUrl;
    private String description;
    private String defaultBranch;
    private Boolean privateRepo;
    private String language;
    private String monitoringStatus;
    private Integer openPrCount;
    private Integer openIssueCount;
    private OffsetDateTime lastSyncedAt;
    private OffsetDateTime createdAt;
    private String qualityStatus;
}
