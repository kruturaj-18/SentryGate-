package com.sentrygate.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "trust_scores")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TrustScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contributor_id", unique = true, nullable = false)
    private Contributor contributor;

    @Column(name = "overall_score", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal overallScore = BigDecimal.valueOf(50.0);

    @Column(name = "pr_success_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal prSuccessScore = BigDecimal.valueOf(50.0);

    @Column(name = "validation_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal validationScore = BigDecimal.valueOf(50.0);

    @Column(name = "collaboration_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal collaborationScore = BigDecimal.valueOf(50.0);

    @Column(name = "consistency_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal consistencyScore = BigDecimal.valueOf(50.0);

    @Column(name = "calculated_at")
    private OffsetDateTime calculatedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
