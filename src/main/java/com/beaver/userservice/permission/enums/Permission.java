package com.beaver.userservice.permission.enums;

public enum Permission {
    // Financial permissions
    TRANSACTION_READ("transaction:read"),
    TRANSACTION_WRITE("transaction:write"),
    BUDGET_READ("budget:read"),
    BUDGET_WRITE("budget:write"),
    REPORT_READ("report:read"),

    // Workspace management
    WORKSPACE_SETTINGS("workspace:settings"),
    WORKSPACE_MEMBERS("workspace:members");

    private final String value;

    Permission(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    // Optional: toString override
    @Override
    public String toString() {
        return value;
    }
}
