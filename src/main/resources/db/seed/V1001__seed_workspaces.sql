-- Seed workspaces for local development
-- Values use Flyway placeholders from application-local.yml

INSERT INTO workspaces (id, created_at, updated_at, name, status, plan, updated_by)
VALUES
    ('${user1_workspace_id}', NOW(), NOW(), '${user1_name}''s Workspace', 'ACTIVE', 'STARTER', '00000000-0000-0000-0000-000000000001'),
    ('${user2_workspace_id}', NOW(), NOW(), '${user2_name}''s Workspace', 'ACTIVE', 'STARTER', '00000000-0000-0000-0000-000000000001')
ON CONFLICT (id) DO NOTHING;
