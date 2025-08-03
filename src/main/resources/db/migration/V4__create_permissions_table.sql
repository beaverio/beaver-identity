-- Permissions
CREATE TABLE permissions (
     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
     code VARCHAR(100) UNIQUE NOT NULL,
     name VARCHAR(255) NOT NULL,
     description TEXT,
     resource VARCHAR(50) NOT NULL,
     action VARCHAR(50) NOT NULL,
     category VARCHAR(50) NOT NULL,
     created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
     updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Insert simplified permissions
INSERT INTO permissions (code, name, description, resource, action, category)
VALUES
      ('deny:all', 'Deny All', 'Deny all operations', 'deny', 'all', 'ADMINISTRATION'),
      ('transaction:read', 'Read Transactions', 'View transaction history and details', 'transaction', 'read', 'FINANCIAL'),
      ('transaction:write', 'Write Transactions', 'Create and edit transactions', 'transaction', 'write', 'FINANCIAL'),
      ('budget:read', 'Read Budgets', 'View budget information', 'budget', 'read', 'FINANCIAL'),
      ('budget:write', 'Write Budgets', 'Create and edit budgets', 'budget', 'write', 'FINANCIAL'),
      ('report:read', 'Read Reports', 'View reports and analytics', 'report', 'read', 'REPORTING'),
      ('workspace:owner', 'Owner Workspace', 'Owner of workspaces information', 'workspace', 'owner', 'ADMINISTRATION'),
      ('workspace:read', 'Read Workspace', 'Read workspaces information', 'workspace', 'read', 'ADMINISTRATION'),
      ('workspace:write', 'Write Workspace', 'Modify workspaces information', 'workspace', 'write', 'ADMINISTRATION');