package com.beaver.identity.workspace.dto;

import com.beaver.identity.membership.entity.WorkspaceMembership;
import lombok.Builder;

import java.util.List;

@Builder
public record WorkspaceListDto(
        String id,
        String name
) {
    public static WorkspaceListDto fromMembership(WorkspaceMembership membership) {
        return WorkspaceListDto.builder()
                .id(membership.getWorkspace().getId().toString())
                .name(membership.getWorkspace().getName())
                .build();
    }

    public static List<WorkspaceListDto> fromMemberships(List<WorkspaceMembership> memberships) {
        return memberships.stream()
                .map(WorkspaceListDto::fromMembership)
                .toList();
    }
}
