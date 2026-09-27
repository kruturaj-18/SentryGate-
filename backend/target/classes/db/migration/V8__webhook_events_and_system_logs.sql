-- V8__webhook_events_and_system_logs.sql

CREATE TABLE webhook_events (
    id                  BIGSERIAL PRIMARY KEY,
    delivery_id         VARCHAR(255) NOT NULL UNIQUE,
    event_type          VARCHAR(100) NOT NULL,
    action              VARCHAR(100),
    repository_id       BIGINT REFERENCES repositories(id) ON DELETE SET NULL,
    github_repo_id      BIGINT,
    payload             JSONB,
    processing_status   VARCHAR(50) DEFAULT 'PENDING',
    error_message       TEXT,
    processed_at        TIMESTAMP WITH TIME ZONE,
    created_at          TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE system_logs (
    id              BIGSERIAL PRIMARY KEY,
    level           VARCHAR(20) NOT NULL,
    logger          VARCHAR(255),
    message         TEXT NOT NULL,
    exception       TEXT,
    context         JSONB DEFAULT '{}',
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_webhook_events_delivery_id ON webhook_events(delivery_id);
CREATE INDEX idx_webhook_events_repo_id ON webhook_events(repository_id);
CREATE INDEX idx_webhook_events_status ON webhook_events(processing_status);
CREATE INDEX idx_webhook_events_created_at ON webhook_events(created_at DESC);
CREATE INDEX idx_system_logs_level ON system_logs(level);
CREATE INDEX idx_system_logs_created_at ON system_logs(created_at DESC);
