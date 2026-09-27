package com.sentrygate.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "issues")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Issue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "github_issue_id", nullable = false)
    private Long githubIssueId;

    @Column(nullable = false)
    private Integer number;

    @Column(nullable = false, length = 1024)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(name = "author_login")
    private String authorLogin;

    @Column(length = 50)
    @Builder.Default
    private String state = "open";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> labels;

    @Column(name = "html_url", length = 1024)
    private String htmlUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    private Repository repository;

    @Column(name = "is_pull_request")
    @Builder.Default
    private Boolean isPullRequest = false;

    @Column(name = "duplicate_status", length = 50)
    @Builder.Default
    private String duplicateStatus = "UNKNOWN";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "duplicate_of_id")
    private Issue duplicateOf;

    @OneToMany(mappedBy = "sourceIssue", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IssueSimilarity> similarities;

    @Column(name = "github_created_at")
    private OffsetDateTime githubCreatedAt;

    @Column(name = "github_updated_at")
    private OffsetDateTime githubUpdatedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
