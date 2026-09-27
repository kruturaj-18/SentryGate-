package com.sentrygate.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "pull_requests")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PullRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "github_pr_id", nullable = false)
    private Long githubPrId;

    @Column(nullable = false)
    private Integer number;

    @Column(nullable = false, length = 1024)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(name = "author_login")
    private String authorLogin;

    @Column(name = "author_github_id")
    private Long authorGithubId;

    @Column(name = "head_branch")
    private String headBranch;

    @Column(name = "base_branch")
    private String baseBranch;

    @Column(name = "head_sha")
    private String headSha;

    @Column(length = 50)
    @Builder.Default
    private String state = "open";

    @Column
    @Builder.Default
    private Boolean draft = false;

    @Column
    @Builder.Default
    private Boolean merged = false;

    @Column
    @Builder.Default
    private Integer additions = 0;

    @Column
    @Builder.Default
    private Integer deletions = 0;

    @Column(name = "changed_files")
    @Builder.Default
    private Integer changedFiles = 0;

    @Column(name = "html_url", length = 1024)
    private String htmlUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    private Repository repository;

    @OneToMany(mappedBy = "pullRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PullRequestValidation> validations;

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
