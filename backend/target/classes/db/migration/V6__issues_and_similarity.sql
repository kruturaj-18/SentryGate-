-- V6__issues_and_similarity.sql

CREATE TABLE issues (
    id                  BIGSERIAL PRIMARY KEY,
    github_issue_id     BIGINT NOT NULL,
    number              INT NOT NULL,
    title               VARCHAR(1024) NOT NULL,
    body                TEXT,
    author_login        VARCHAR(255),
    state               VARCHAR(50) DEFAULT 'open',
    labels              JSONB DEFAULT '[]',
    html_url            VARCHAR(1024),
    repository_id       BIGINT NOT NULL REFERENCES repositories(id) ON DELETE CASCADE,
    is_pull_request     BOOLEAN DEFAULT FALSE,
    duplicate_status    VARCHAR(50) DEFAULT 'UNKNOWN',
    duplicate_of_id     BIGINT REFERENCES issues(id),
    github_created_at   TIMESTAMP WITH TIME ZONE,
    github_updated_at   TIMESTAMP WITH TIME ZONE,
    created_at          TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at          TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(repository_id, number)
);

CREATE TABLE issue_similarities (
    id                  BIGSERIAL PRIMARY KEY,
    source_issue_id     BIGINT NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
    target_issue_id     BIGINT NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
    similarity_score    DECIMAL(5,4) NOT NULL,
    method              VARCHAR(100) DEFAULT 'COSINE_TF_IDF',
    created_at          TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(source_issue_id, target_issue_id)
);

CREATE INDEX idx_issues_repo_id ON issues(repository_id);
CREATE INDEX idx_issues_state ON issues(state);
CREATE INDEX idx_issues_github_id ON issues(github_issue_id);
CREATE INDEX idx_issue_similarities_source ON issue_similarities(source_issue_id);
CREATE INDEX idx_issue_similarities_target ON issue_similarities(target_issue_id);
