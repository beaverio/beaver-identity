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
      ('transaction:read', 'Read Transactions', 'View transaction history and details', 'transaction', 'read', 'FINANCIAL'),
      ('transaction:write', 'Write Transactions', 'Create and edit transactions', 'transaction', 'write', 'FINANCIAL'),
      ('budget:read', 'Read Budgets', 'View budget information', 'budget', 'read', 'FINANCIAL'),
      ('budget:write', 'Write Budgets', 'Create and edit budgets', 'budget', 'write', 'FINANCIAL'),
      ('report:read', 'Read Reports', 'View reports and analytics', 'report', 'read', 'REPORTING'),
      ('workspace:settings', 'Workspace Settings', 'Modify workspace settings', 'workspace', 'settings', 'ADMINISTRATION'),
      ('workspace:members', 'Manage Members', 'Add and remove workspace members', 'workspace', 'members', 'ADMINISTRATION');