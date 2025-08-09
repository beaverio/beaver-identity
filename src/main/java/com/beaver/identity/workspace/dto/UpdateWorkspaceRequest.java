package com.beaver.identity.workspace.dto;

import lombok.Builder;

@Builder
public record UpdateWorkspaceRequest(
        String name
) {
}
