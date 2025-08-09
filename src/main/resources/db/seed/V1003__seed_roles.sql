-- Seed roles for workspaces
-- Creates standard RBAC roles for each workspace using Flyway placeholders

INSERT INTO roles (id, created_at, updated_at, workspace_id, role_type)
VALUES
    (gen_random_uuid(), NOW(), NOW(), '${user1_workspace_id}', 'OWNER'),
    (gen_random_uuid(), NOW(), NOW(), '${user1_workspace_id}', 'WRITE'),
    (gen_random_uuid(), NOW(), NOW(), '${user1_workspace_id}', 'READ'),
    (gen_random_uuid(), NOW(), NOW(), '${user2_workspace_id}', 'OWNER'),
    (gen_random_uuid(), NOW(), NOW(), '${user2_workspace_id}', 'WRITE'),
    (gen_random_uuid(), NOW(), NOW(), '${user2_workspace_id}', 'READ')
ON CONFLICT (workspace_id, role_type) DO NOTHING;
