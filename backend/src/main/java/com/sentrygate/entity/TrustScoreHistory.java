package com.sentrygate.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "trust_score_history")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TrustScoreHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contributor_id", nullable = false)
    private Contributor contributor;

    @Column(name = "overall_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal overallScore;

    @Column(name = "pr_success_score", precision = 5, scale = 2)
    private BigDecimal prSuccessScore;

    @Column(name = "validation_score", precision = 5, scale = 2)
    private BigDecimal validationScore;

    @Column(name = "collaboration_score", precision = 5, scale = 2)
    private BigDecimal collaborationScore;

    @Column(name = "consistency_score", precision = 5, scale = 2)
    private BigDecimal consistencyScore;

    @Column(name = "recorded_at")
    @CreationTimestamp
    private OffsetDateTime recordedAt;
}
