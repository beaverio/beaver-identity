package com.beaver.userservice.permission.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Permission {
    DENY_ALL("deny:read"),

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

    @Override
    public String toString() {
        return value;
    }
}
