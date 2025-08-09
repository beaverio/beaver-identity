package com.beaver.identity.membership.dto;

import com.beaver.identity.common.dto.BaseDto;
import com.beaver.identity.membership.enums.MembershipStatus;
import com.beaver.identity.user.dto.UserDto;
import com.beaver.identity.workspace.dto.WorkspaceDto;
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
    private String role;
    private Set<String> permissions;
    private MembershipStatus status;
    private LocalDateTime joinedAt;
}
