-- V5__contributors_and_trust_scores.sql

CREATE TABLE contributors (
    id                  BIGSERIAL PRIMARY KEY,
    github_id           BIGINT NOT NULL,
    login               VARCHAR(255) NOT NULL,
    name                VARCHAR(255),
    avatar_url          VARCHAR(1024),
    email               VARCHAR(255),
    repository_id       BIGINT NOT NULL REFERENCES repositories(id) ON DELETE CASCADE,
    total_prs           INT DEFAULT 0,
    merged_prs          INT DEFAULT 0,
    rejected_prs        INT DEFAULT 0,
    open_prs            INT DEFAULT 0,
    total_issues        INT DEFAULT 0,
    closed_issues       INT DEFAULT 0,
    validation_passes   INT DEFAULT 0,
    validation_failures INT DEFAULT 0,
    contribution_count  INT DEFAULT 0,
    first_contribution_at TIMESTAMP WITH TIME ZONE,
    last_contribution_at  TIMESTAMP WITH TIME ZONE,
    created_at          TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at          TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(github_id, repository_id)
);

CREATE TABLE trust_scores (
    id                      BIGSERIAL PRIMARY KEY,
    contributor_id          BIGINT NOT NULL REFERENCES contributors(id) ON DELETE CASCADE UNIQUE,
    overall_score           DECIMAL(5,2) NOT NULL DEFAULT 50.0,
    pr_success_score        DECIMAL(5,2) DEFAULT 50.0,
    validation_score        DECIMAL(5,2) DEFAULT 50.0,
    collaboration_score     DECIMAL(5,2) DEFAULT 50.0,
    consistency_score       DECIMAL(5,2) DEFAULT 50.0,
    calculated_at           TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    created_at              TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at              TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE trust_score_history (
    id                  BIGSERIAL PRIMARY KEY,
    contributor_id      BIGINT NOT NULL REFERENCES contributors(id) ON DELETE CASCADE,
    overall_score       DECIMAL(5,2) NOT NULL,
    pr_success_score    DECIMAL(5,2),
    validation_score    DECIMAL(5,2),
    collaboration_score DECIMAL(5,2),
    consistency_score   DECIMAL(5,2),
    recorded_at         TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_contributors_repo_id ON contributors(repository_id);
CREATE INDEX idx_contributors_github_id ON contributors(github_id);
CREATE INDEX idx_trust_scores_contributor ON trust_scores(contributor_id);
CREATE INDEX idx_trust_history_contributor ON trust_score_history(contributor_id);
CREATE INDEX idx_trust_history_recorded_at ON trust_score_history(recorded_at);
