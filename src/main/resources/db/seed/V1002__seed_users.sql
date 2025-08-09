-- Seed users for local development
-- Values use Flyway placeholders from application-local.yml
-- Password for both users: "password"

INSERT INTO users (id, created_at, updated_at, email, password, name, is_active, last_workspace_id)
VALUES
    ('${user1_id}', NOW(), NOW(), '${user1_email}', '${user1_password}', '${user1_name}', true, '${user1_workspace_id}'),
    ('${user2_id}', NOW(), NOW(), '${user2_email}', '${user2_password}', '${user2_name}', true, '${user2_workspace_id}')
ON CONFLICT (id) DO NOTHING;
