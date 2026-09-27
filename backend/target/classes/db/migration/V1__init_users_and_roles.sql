-- V1__init_users_and_roles.sql
-- Initial schema: users, roles, and user-role mapping

CREATE TABLE roles (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(50) NOT NULL UNIQUE,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    github_id       BIGINT UNIQUE NOT NULL,
    login           VARCHAR(255) NOT NULL UNIQUE,
    name            VARCHAR(255),
    email           VARCHAR(255),
    avatar_url      VARCHAR(1024),
    github_token    TEXT,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    last_login_at   TIMESTAMP WITH TIME ZONE
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- Insert default roles
INSERT INTO roles (name) VALUES ('ROLE_MAINTAINER'), ('ROLE_ADMIN');

CREATE INDEX idx_users_github_id ON users(github_id);
CREATE INDEX idx_users_login ON users(login);
