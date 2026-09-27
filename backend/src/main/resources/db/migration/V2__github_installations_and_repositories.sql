-- V2__github_installations_and_repositories.sql

CREATE TABLE github_installations (
    id                  BIGSERIAL PRIMARY KEY,
    installation_id     BIGINT NOT NULL UNIQUE,
    account_login       VARCHAR(255) NOT NULL,
    account_type        VARCHAR(50) NOT NULL,
    app_id              BIGINT,
    access_token        TEXT,
    token_expires_at    TIMESTAMP WITH TIME ZONE,
    installed_by_user_id BIGINT REFERENCES users(id),
    created_at          TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at          TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE repositories (
    id                  BIGSERIAL PRIMARY KEY,
    github_repo_id      BIGINT NOT NULL UNIQUE,
    name                VARCHAR(255) NOT NULL,
    full_name           VARCHAR(512) NOT NULL,
    owner               VARCHAR(255) NOT NULL,
    url                 VARCHAR(1024),
    html_url            VARCHAR(1024),
    description         TEXT,
    default_branch      VARCHAR(255) DEFAULT 'main',
    private_repo        BOOLEAN DEFAULT FALSE,
    language            VARCHAR(100),
    installation_id     BIGINT REFERENCES github_installations(id),
    connected_user_id   BIGINT REFERENCES users(id),
    monitoring_status   VARCHAR(50) DEFAULT 'ACTIVE',
    open_pr_count       INT DEFAULT 0,
    open_issue_count    INT DEFAULT 0,
    last_synced_at      TIMESTAMP WITH TIME ZONE,
    created_at          TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at          TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_repos_github_id ON repositories(github_repo_id);
CREATE INDEX idx_repos_full_name ON repositories(full_name);
CREATE INDEX idx_repos_owner ON repositories(owner);
CREATE INDEX idx_github_installations_install_id ON github_installations(installation_id);
