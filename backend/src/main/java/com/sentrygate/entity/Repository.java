package com.sentrygate.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "repositories")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Repository {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "github_repo_id", unique = true, nullable = false)
    private Long githubRepoId;

    @Column(nullable = false)
    private String name;

    @Column(name = "full_name", nullable = false, length = 512)
    private String fullName;

    @Column(nullable = false)
    private String owner;

    @Column(length = 1024)
    private String url;

    @Column(name = "html_url", length = 1024)
    private String htmlUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "default_branch")
    private String defaultBranch;

    @Column(name = "private_repo")
    private Boolean privateRepo;

    @Column
    private String language;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "installation_id")
    private GitHubInstallation installation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "connected_user_id")
    private User connectedUser;

    @Column(name = "monitoring_status", length = 50)
    @Builder.Default
    private String monitoringStatus = "ACTIVE";

    @Column(name = "open_pr_count")
    @Builder.Default
    private Integer openPrCount = 0;

    @Column(name = "open_issue_count")
    @Builder.Default
    private Integer openIssueCount = 0;

    @Column(name = "last_synced_at")
    private OffsetDateTime lastSyncedAt;

    @OneToMany(mappedBy = "repository", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RepositoryRule> rules;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
