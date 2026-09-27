package com.sentrygate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Map;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ActivityLogDto {
    private Long id;
    private String actorLogin;
    private Long actorUserId;
    private String eventType;
    private Long repositoryId;
    private String repositoryFullName;
    private String entityType;
    private Long entityId;
    private String description;
    private Map<String, Object> metadata;
    private OffsetDateTime createdAt;
}
