-- V7__notifications_and_activity.sql

CREATE TABLE notifications (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type            VARCHAR(100) NOT NULL,
    title           VARCHAR(512) NOT NULL,
    message         TEXT,
    is_read         BOOLEAN DEFAULT FALSE,
    entity_type     VARCHAR(100),
    entity_id       BIGINT,
    repository_id   BIGINT REFERENCES repositories(id) ON DELETE SET NULL,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE activity_logs (
    id              BIGSERIAL PRIMARY KEY,
    actor_login     VARCHAR(255),
    actor_user_id   BIGINT REFERENCES users(id) ON DELETE SET NULL,
    event_type      VARCHAR(100) NOT NULL,
    repository_id   BIGINT REFERENCES repositories(id) ON DELETE SET NULL,
    entity_type     VARCHAR(100),
    entity_id       BIGINT,
    description     TEXT NOT NULL,
    metadata        JSONB DEFAULT '{}',
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_is_read ON notifications(is_read);
CREATE INDEX idx_notifications_created_at ON notifications(created_at DESC);
CREATE INDEX idx_activity_logs_actor ON activity_logs(actor_user_id);
CREATE INDEX idx_activity_logs_repo ON activity_logs(repository_id);
CREATE INDEX idx_activity_logs_created_at ON activity_logs(created_at DESC);
CREATE INDEX idx_activity_logs_event_type ON activity_logs(event_type);
