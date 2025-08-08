-- Workspace Memberships
CREATE TABLE workspace_memberships (
       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
       user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
       workspace_id UUID NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
       role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
       status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'SUSPENDED', 'PENDING')),
       joined_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
       created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
       updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
       UNIQUE(user_id, workspace_id)
);

-- Create indexes for performance
CREATE INDEX idx_workspace_memberships_user_id ON workspace_memberships(user_id);
CREATE INDEX idx_workspace_memberships_workspace_id ON workspace_memberships(workspace_id);
CREATE INDEX idx_workspace_memberships_role_id ON workspace_memberships(role_id);
CREATE INDEX idx_workspace_memberships_status ON workspace_memberships(status);
