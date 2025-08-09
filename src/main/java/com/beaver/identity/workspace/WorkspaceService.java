package com.beaver.identity.workspace;

import com.beaver.auth.exceptions.AccessDeniedException;
import com.beaver.auth.jwt.AccessToken;
import com.beaver.auth.jwt.JwtService;
import com.beaver.auth.jwt.RefreshToken;
import com.beaver.auth.roles.Role;
import com.beaver.identity.common.exception.NotFoundException;
import com.beaver.identity.membership.MembershipService;
import com.beaver.identity.membership.entity.WorkspaceMembership;
import com.beaver.identity.role.service.WorkspaceRoleService;
import com.beaver.identity.user.UserService;
import com.beaver.identity.user.dto.UpdateUser;
import com.beaver.identity.user.entity.User;
import com.beaver.identity.workspace.dto.CreateWorkspaceRequest;
import com.beaver.identity.workspace.enums.PlanType;
import com.beaver.identity.workspace.entity.Workspace;
import com.beaver.identity.workspace.enums.WorkspaceStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Transactional
@Slf4j
@Service
public class WorkspaceService {

    private final IWorkspaceRepository workspaceRepository;
    private final MembershipService membershipService;
    private final UserService userService;
    private final JwtService jwtService;
    private final WorkspaceRoleService roleService;

    public Workspace createWorkspace(CreateWorkspaceRequest request, UUID ownerId) {
        log.info("Creating workspace '{}' for user: {}", request.name(), ownerId);

        Workspace workspace = Workspace.builder()
                .name(request.name())
                .status(WorkspaceStatus.ACTIVE)
                .plan(PlanType.STARTER)
                .build();

        workspace = workspaceRepository.save(workspace);
        log.info("Created workspace with ID: {}", workspace.getId());

        roleService.createDefaultRoles(workspace.getId(), workspace);

        User owner = userService.findById(ownerId);
        WorkspaceMembership membership = membershipService.addUserToWorkspace(owner, workspace, Role.OWNER);
        log.info("Added user {} as owner of workspace {} with membership {}", ownerId, workspace.getId(), membership.getId());

        return workspace;
    }

    public Workspace findById(UUID workspaceId) {
        return workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new NotFoundException("Workspace not found: " + workspaceId));
    }

    public WorkspaceMembership createDefaultWorkspace(User user) {
        log.info("Creating default workspace for user: {}", user.getId());

        String workspaceName = user.getName() + "'s Workspace";
        Workspace workspace = Workspace.builder()
                .name(workspaceName)
                .status(WorkspaceStatus.ACTIVE)
                .plan(PlanType.STARTER)
                .build();

        workspace = workspaceRepository.save(workspace);
        log.info("Created default workspace with ID: {}", workspace.getId());

        roleService.createDefaultRoles(workspace.getId(), workspace);

        WorkspaceMembership membership = membershipService.addUserToWorkspace(user, workspace, Role.OWNER);
        log.info("Added user {} as owner of default workspace {} with membership {}",
                user.getId(), workspace.getId(), membership.getId());

        return membership;
    }

    public Map<String, String> switchWorkspace(UUID userId, UUID workspaceId) {
        log.info("Attempting to switch workspace '{}' for user '{}'", workspaceId, userId);

        User user = userService.findById(userId);

        List<WorkspaceMembership> memberships = membershipService.findActiveByUserId(user.getId());
        WorkspaceMembership membership = memberships.stream()
                .filter(m -> m.getWorkspace().getId().equals(workspaceId))
                .findFirst()
                .orElseThrow(() -> new AccessDeniedException("User does not have access to this workspace"));

        log.info("Access granted for workspace '{}' for user '{}'", workspaceId, userId);
        userService.updateUser(user.getId(), UpdateUser.builder().lastWorkspaceId(membership.getWorkspace().getId()).build());

        String newAccessToken = jwtService.generateAccessToken(
                AccessToken.builder()
                        .userId(user.getId().toString())
                        .email(user.getEmail())
                        .name(user.getName())
                        .workspaceId(membership.getWorkspace().getId().toString())
                        .role(membership.getRole().toString())
                        .build()
        );

        String newRefreshToken = jwtService.generateRefreshToken(
                RefreshToken.builder()
                        .userId(user.getId().toString())
                        .workspaceId(membership.getWorkspace().getId().toString())
                        .build()
        );

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", newAccessToken);
        tokens.put("refreshToken", newRefreshToken);

        return tokens;
    }
}
