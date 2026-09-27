package com.sentrygate.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "contributors")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Contributor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "github_id", nullable = false)
    private Long githubId;

    @Column(nullable = false)
    private String login;

    @Column
    private String name;

    @Column(name = "avatar_url", length = 1024)
    private String avatarUrl;

    @Column
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    private Repository repository;

    @Column(name = "total_prs")
    @Builder.Default
    private Integer totalPrs = 0;

    @Column(name = "merged_prs")
    @Builder.Default
    private Integer mergedPrs = 0;

    @Column(name = "rejected_prs")
    @Builder.Default
    private Integer rejectedPrs = 0;

    @Column(name = "open_prs")
    @Builder.Default
    private Integer openPrs = 0;

    @Column(name = "total_issues")
    @Builder.Default
    private Integer totalIssues = 0;

    @Column(name = "closed_issues")
    @Builder.Default
    private Integer closedIssues = 0;

    @Column(name = "validation_passes")
    @Builder.Default
    private Integer validationPasses = 0;

    @Column(name = "validation_failures")
    @Builder.Default
    private Integer validationFailures = 0;

    @Column(name = "contribution_count")
    @Builder.Default
    private Integer contributionCount = 0;

    @Column(name = "first_contribution_at")
    private OffsetDateTime firstContributionAt;

    @Column(name = "last_contribution_at")
    private OffsetDateTime lastContributionAt;

    @OneToOne(mappedBy = "contributor", cascade = CascadeType.ALL)
    private TrustScore trustScore;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
