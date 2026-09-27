-- V4__pull_requests_and_validations.sql

CREATE TABLE pull_requests (
    id                  BIGSERIAL PRIMARY KEY,
    github_pr_id        BIGINT NOT NULL,
    number              INT NOT NULL,
    title               VARCHAR(1024) NOT NULL,
    body                TEXT,
    author_login        VARCHAR(255),
    author_github_id    BIGINT,
    head_branch         VARCHAR(255),
    base_branch         VARCHAR(255),
    head_sha            VARCHAR(255),
    state               VARCHAR(50) DEFAULT 'open',
    draft               BOOLEAN DEFAULT FALSE,
    merged              BOOLEAN DEFAULT FALSE,
    additions           INT DEFAULT 0,
    deletions           INT DEFAULT 0,
    changed_files       INT DEFAULT 0,
    html_url            VARCHAR(1024),
    repository_id       BIGINT NOT NULL REFERENCES repositories(id) ON DELETE CASCADE,
    github_created_at   TIMESTAMP WITH TIME ZONE,
    github_updated_at   TIMESTAMP WITH TIME ZONE,
    created_at          TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at          TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(repository_id, number)
);

CREATE TABLE pull_request_validations (
    id                  BIGSERIAL PRIMARY KEY,
    pull_request_id     BIGINT NOT NULL REFERENCES pull_requests(id) ON DELETE CASCADE,
    overall_status      VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    pass_count          INT DEFAULT 0,
    fail_count          INT DEFAULT 0,
    warning_count       INT DEFAULT 0,
    skipped_count       INT DEFAULT 0,
    triggered_by        VARCHAR(100) DEFAULT 'WEBHOOK',
    started_at          TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    completed_at        TIMESTAMP WITH TIME ZONE,
    created_at          TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE validation_results (
    id                      BIGSERIAL PRIMARY KEY,
    validation_id           BIGINT NOT NULL REFERENCES pull_request_validations(id) ON DELETE CASCADE,
    rule_code               VARCHAR(100) NOT NULL,
    rule_name               VARCHAR(255) NOT NULL,
    status                  VARCHAR(50) NOT NULL,
    severity                VARCHAR(50) NOT NULL DEFAULT 'INFO',
    message                 TEXT,
    details                 JSONB DEFAULT '{}',
    created_at              TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_prs_repo_id ON pull_requests(repository_id);
CREATE INDEX idx_prs_github_id ON pull_requests(github_pr_id);
CREATE INDEX idx_prs_state ON pull_requests(state);
CREATE INDEX idx_pr_validations_pr_id ON pull_request_validations(pull_request_id);
CREATE INDEX idx_validation_results_validation_id ON validation_results(validation_id);
