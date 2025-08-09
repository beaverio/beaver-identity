-- Seed roles for workspaces
-- Creates standard RBAC roles for each workspace using Flyway placeholders

INSERT INTO roles (id, created_at, updated_at, workspace_id, role_type, updated_by)
VALUES
    (gen_random_uuid(), NOW(), NOW(), '${user1_workspace_id}', 'OWNER', '00000000-0000-0000-0000-000000000001'),
    (gen_random_uuid(), NOW(), NOW(), '${user1_workspace_id}', 'WRITE', '00000000-0000-0000-0000-000000000001'),
    (gen_random_uuid(), NOW(), NOW(), '${user1_workspace_id}', 'READ', '00000000-0000-0000-0000-000000000001'),
    (gen_random_uuid(), NOW(), NOW(), '${user2_workspace_id}', 'OWNER', '00000000-0000-0000-0000-000000000001'),
    (gen_random_uuid(), NOW(), NOW(), '${user2_workspace_id}', 'WRITE', '00000000-0000-0000-0000-000000000001'),
    (gen_random_uuid(), NOW(), NOW(), '${user2_workspace_id}', 'READ', '00000000-0000-0000-0000-000000000001')
ON CONFLICT (workspace_id, role_type) DO NOTHING;
