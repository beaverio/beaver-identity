-- Add user read and write permissions
INSERT INTO permissions (code, name, description, resource, action, category)
VALUES
    ('user:read', 'Read Users', 'View user profiles and information', 'user', 'read', 'USER_MANAGEMENT'),
    ('user:write', 'Write Users', 'Create and edit user profiles', 'user', 'write', 'USER_MANAGEMENT');