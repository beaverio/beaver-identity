-- Create RBAC roles table for workspace-scoped role-based access control
CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workspace_id UUID NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    role_type VARCHAR(10) NOT NULL CHECK (role_type IN ('READ', 'WRITE', 'ADMIN', 'OWNER')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(workspace_id, role_type)
);

-- Create indexes for performance
CREATE INDEX idx_roles_workspace_type ON roles(workspace_id, role_type);
CREATE INDEX idx_roles_workspace_id ON roles(workspace_id);
CREATE INDEX idx_roles_type ON roles(role_type);
