-- Seed system user for audit tracking
INSERT INTO users (
    id,
    created_at,
    updated_at,
    email,
    password,
    name,
    is_active,
    last_workspace_id,
    updated_by
) VALUES (
    '00000000-0000-0000-0000-000000000001',
    NOW(),
    NOW(),
    'system@beaver.internal',
    '$2a$10$disabled.system.user.password.hash',
    'System User',
    false,
    null,
    '00000000-0000-0000-0000-000000000001'
) ON CONFLICT (id) DO NOTHING;

