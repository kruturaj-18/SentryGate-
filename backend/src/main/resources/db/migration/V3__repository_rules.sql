-- V3__repository_rules.sql

CREATE TABLE repository_rules (
    id                  BIGSERIAL PRIMARY KEY,
    repository_id       BIGINT NOT NULL REFERENCES repositories(id) ON DELETE CASCADE,
    rule_code           VARCHAR(100) NOT NULL,
    rule_name           VARCHAR(255) NOT NULL,
    enabled             BOOLEAN DEFAULT TRUE,
    severity            VARCHAR(50) DEFAULT 'MEDIUM',
    configuration       JSONB DEFAULT '{}',
    created_at          TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at          TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(repository_id, rule_code)
);

CREATE INDEX idx_repo_rules_repo_id ON repository_rules(repository_id);
CREATE INDEX idx_repo_rules_code ON repository_rules(rule_code);
