package com.beaver.identity.user.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record UpdateSelf(
        String name,
        UUID lastWorkspaceId
) {
}
