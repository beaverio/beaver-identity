package com.beaver.identity.workspace.dto;

import java.util.UUID;

public record SwitchWorkspaceRequest(
        UUID workspaceId
) {
}
