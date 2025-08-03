package com.beaver.identity.auth.dto;

import com.beaver.identity.membership.dto.WorkspaceMembershipDto;
import com.beaver.identity.membership.entity.WorkspaceMembership;
import com.beaver.identity.user.dto.UserDto;
import com.beaver.identity.user.entity.User;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class UserWithWorkspacesDto {
    private UserDto user;
    private List<WorkspaceMembershipDto> workspaces;

    public static UserWithWorkspacesDto fromUserAndMemberships(User user, List<WorkspaceMembership> memberships) {
        return UserWithWorkspacesDto.builder()
                .user(UserDto.fromEntity(user))
                .workspaces(memberships.stream()
                        .map(WorkspaceMembershipDto::fromEntity)
                        .collect(Collectors.toList()))
                .build();
    }
}
