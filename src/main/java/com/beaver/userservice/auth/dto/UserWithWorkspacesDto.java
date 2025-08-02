package com.beaver.userservice.auth.dto;

import com.beaver.userservice.membership.dto.WorkspaceMembershipDto;
import com.beaver.userservice.membership.entity.WorkspaceMembership;
import com.beaver.userservice.user.dto.UserDto;
import com.beaver.userservice.user.entity.User;
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
