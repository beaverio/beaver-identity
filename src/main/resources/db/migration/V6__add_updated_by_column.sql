-- Add updated_by column to all tables that extend BaseEntity

-- Add updated_by to users table
ALTER TABLE users ADD COLUMN updated_by UUID;

-- Add updated_by to workspaces table
ALTER TABLE workspaces ADD COLUMN updated_by UUID;

-- Add updated_by to roles table
ALTER TABLE roles ADD COLUMN updated_by UUID;

-- Add updated_by to workspace_memberships table
ALTER TABLE workspace_memberships ADD COLUMN updated_by UUID;

-- Set default value for existing records to system user
UPDATE users SET updated_by = '00000000-0000-0000-0000-000000000001' WHERE updated_by IS NULL;
UPDATE workspaces SET updated_by = '00000000-0000-0000-0000-000000000001' WHERE updated_by IS NULL;
UPDATE roles SET updated_by = '00000000-0000-0000-0000-000000000001' WHERE updated_by IS NULL;
UPDATE workspace_memberships SET updated_by = '00000000-0000-0000-0000-000000000001' WHERE updated_by IS NULL;
