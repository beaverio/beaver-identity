package com.beaver.identity.user.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record UpdateUser(
        String name,
        UUID lastWorkspaceId
) {
}
