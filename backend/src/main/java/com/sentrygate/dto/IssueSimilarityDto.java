package com.sentrygate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class IssueSimilarityDto {
    private Long id;
    private Long sourceIssueId;
    private Long targetIssueId;
    private Integer targetIssueNumber;
    private String targetIssueTitle;
    private BigDecimal similarityScore;
    private String method;
    private OffsetDateTime createdAt;
}
