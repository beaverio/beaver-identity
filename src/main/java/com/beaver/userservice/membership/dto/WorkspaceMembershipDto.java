package com.beaver.userservice.membership.dto;

import com.beaver.userservice.common.dto.BaseDto;
import com.beaver.userservice.membership.enums.MembershipStatus;
import com.beaver.userservice.membership.entity.WorkspaceMembership;
import com.beaver.userservice.user.dto.UserDto;
import com.beaver.userservice.workspace.dto.WorkspaceDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class WorkspaceMembershipDto extends BaseDto {
    private UserDto user;
    private WorkspaceDto workspace;
    private String roleName;
    private Set<String> permissions;
    private MembershipStatus status;
    private LocalDateTime joinedAt;

    public static WorkspaceMembershipDto fromEntity(WorkspaceMembership membership) {
        return WorkspaceMembershipDto.builder()
                .id(membership.getId())
                .user(UserDto.fromEntity(membership.getUser()))
                .workspace(WorkspaceDto.fromEntity(membership.getWorkspace()))
                .roleName(membership.getRole().getName())
                .permissions(membership.getAllPermissionCodes())
                .status(membership.getStatus())
                .joinedAt(membership.getJoinedAt())
                .createdAt(membership.getCreatedAt())
                .updatedAt(membership.getUpdatedAt())
                .build();
    }

    public Set<String> getAllPermissions() {
        return permissions;
    }
}
