-- Seed workspaces for local development
-- Values use Flyway placeholders from application-local.yml

INSERT INTO workspaces (id, created_at, updated_at, name, status, plan)
VALUES
    ('${user1_workspace_id}', NOW(), NOW(), '${user1_name}''s Workspace', 'ACTIVE', 'STARTER'),
    ('${user2_workspace_id}', NOW(), NOW(), '${user2_name}''s Workspace', 'ACTIVE', 'STARTER')
ON CONFLICT (id) DO NOTHING;
