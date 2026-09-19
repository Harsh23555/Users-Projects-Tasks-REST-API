-- V2__seed_data.sql
-- Development seed data — safe to delete in production

-- Admin user  (password: Admin@1234)
INSERT INTO users (name, email, password, role, created_at, updated_at)
VALUES ('Admin User', 'admin@example.com',
        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
        'ADMIN', NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

-- Regular user  (password: User@1234)
INSERT INTO users (name, email, password, role, created_at, updated_at)
VALUES ('John Doe', 'john@example.com',
        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
        'USER', NOW(), NOW())
ON CONFLICT (email) DO NOTHING;
