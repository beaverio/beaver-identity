package com.beaver.identity.membership.dto;

import com.beaver.auth.roles.Role;
import com.beaver.identity.common.dto.BaseDto;
import com.beaver.identity.membership.entity.WorkspaceMembership;
import com.beaver.identity.membership.enums.MembershipStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class WorkspaceMembershipDto extends BaseDto {
    private UUID workspaceId;
    private String workspaceName;
    private Role role;
    private MembershipStatus status;
    private LocalDateTime joinedAt;

    public static WorkspaceMembershipDto fromEntity(WorkspaceMembership membership) {
        return WorkspaceMembershipDto.builder()
                .id(membership.getId())
                .createdAt(membership.getCreatedAt())
                .updatedAt(membership.getUpdatedAt())
                .updatedBy(membership.getUpdatedBy())
                .workspaceId(membership.getWorkspace().getId())
                .workspaceName(membership.getWorkspace().getName())
                .role(membership.getRole().getRoleType())
                .status(membership.getStatus())
                .joinedAt(membership.getJoinedAt())
                .build();
    }
}
