-- Seed workspace memberships for local development
-- Assigns users to workspaces with specific roles using Flyway placeholders

INSERT INTO workspace_memberships (id, created_at, updated_at, user_id, workspace_id, role_id, status, joined_at, updated_by)
SELECT
    gen_random_uuid(),
    NOW(),
    NOW(),
    '${user1_id}',
    '${user1_workspace_id}',
    r.id,
    'ACTIVE',
    NOW(),
    '00000000-0000-0000-0000-000000000001'
FROM roles r
WHERE r.workspace_id = '${user1_workspace_id}'
  AND r.role_type = '${user1_role}'
  AND NOT EXISTS (
    SELECT 1 FROM workspace_memberships wm
    WHERE wm.user_id = '${user1_id}'
      AND wm.workspace_id = '${user1_workspace_id}'
  );

INSERT INTO workspace_memberships (id, created_at, updated_at, user_id, workspace_id, role_id, status, joined_at, updated_by)
SELECT
    gen_random_uuid(),
    NOW(),
    NOW(),
    '${user2_id}',
    '${user2_workspace_id}',
    r.id,
    'ACTIVE',
    NOW(),
    '00000000-0000-0000-0000-000000000001'
FROM roles r
WHERE r.workspace_id = '${user2_workspace_id}'
  AND r.role_type = '${user2_role}'
  AND NOT EXISTS (
    SELECT 1 FROM workspace_memberships wm
    WHERE wm.user_id = '${user2_id}'
      AND wm.workspace_id = '${user2_workspace_id}'
  );
