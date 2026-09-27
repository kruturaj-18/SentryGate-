package com.sentrygate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TrustScoreHistoryDto {
    private Long id;
    private Long contributorId;
    private BigDecimal overallScore;
    private BigDecimal prSuccessScore;
    private BigDecimal validationScore;
    private BigDecimal collaborationScore;
    private BigDecimal consistencyScore;
    private OffsetDateTime recordedAt;
}
