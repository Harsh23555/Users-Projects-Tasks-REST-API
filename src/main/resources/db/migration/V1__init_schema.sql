-- V1__init_schema.sql
-- Task Management API — initial schema

CREATE TABLE IF NOT EXISTS users (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100)  NOT NULL,
    email       VARCHAR(255)  NOT NULL UNIQUE,
    password    VARCHAR(255)  NOT NULL,
    role        VARCHAR(20)   NOT NULL DEFAULT 'USER',
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

CREATE TABLE IF NOT EXISTS projects (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(200)  NOT NULL,
    description VARCHAR(1000),
    status      VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
    owner_id    BIGINT        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_projects_owner_id ON projects(owner_id);
CREATE INDEX IF NOT EXISTS idx_projects_status   ON projects(status);

CREATE TABLE IF NOT EXISTS tasks (
    id               BIGSERIAL PRIMARY KEY,
    title            VARCHAR(200)  NOT NULL,
    description      VARCHAR(2000),
    status           VARCHAR(20)   NOT NULL DEFAULT 'TODO',
    priority         VARCHAR(20)   NOT NULL DEFAULT 'MEDIUM',
    due_date         TIMESTAMP,
    project_id       BIGINT        NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    assigned_user_id BIGINT        REFERENCES users(id) ON DELETE SET NULL,
    created_at       TIMESTAMP,
    updated_at       TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_tasks_project_id       ON tasks(project_id);
CREATE INDEX IF NOT EXISTS idx_tasks_assigned_user_id ON tasks(assigned_user_id);
CREATE INDEX IF NOT EXISTS idx_tasks_status           ON tasks(status);
CREATE INDEX IF NOT EXISTS idx_tasks_priority         ON tasks(priority);
