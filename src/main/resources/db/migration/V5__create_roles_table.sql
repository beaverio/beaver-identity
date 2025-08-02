-- Simplified Roles (only Owner and Viewer)
CREATE TABLE roles (
       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
       workspace_id UUID NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
       name VARCHAR(100) NOT NULL,
       description TEXT,
       is_system_role BOOLEAN NOT NULL DEFAULT false,
       created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
       updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
       UNIQUE(workspace_id, name)
);

-- Role-Permission junction
CREATE TABLE role_permissions (
      role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
      permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
      PRIMARY KEY (role_id, permission_id)
);