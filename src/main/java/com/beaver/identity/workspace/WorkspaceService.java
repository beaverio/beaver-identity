package com.beaver.identity.workspace;

import com.beaver.auth.exceptions.AccessDeniedException;
import com.beaver.auth.jwt.AccessToken;
import com.beaver.auth.jwt.JwtService;
import com.beaver.identity.membership.MembershipService;
import com.beaver.identity.membership.entity.WorkspaceMembership;
import com.beaver.identity.permission.RoleService;
import com.beaver.identity.permission.entity.Permission;
import com.beaver.identity.permission.entity.Role;
import com.beaver.identity.user.UserService;
import com.beaver.identity.user.entity.User;
import com.beaver.identity.workspace.dto.CreateWorkspaceRequest;
import com.beaver.identity.workspace.enums.PlanType;
import com.beaver.identity.workspace.entity.Workspace;
import com.beaver.identity.workspace.enums.WorkspaceStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class WorkspaceService {

    private final IWorkspaceRepository workspaceRepository;
    private final RoleService roleService;
    private final MembershipService membershipService;
    private final UserService userService;
    private final JwtService jwtService;

    public Workspace createWorkspace(CreateWorkspaceRequest request, UUID ownerId) {
        log.info("Creating workspace '{}' for user: {}", request.name(), ownerId);

        Workspace workspace = Workspace.builder()
                .name(request.name())
                .status(WorkspaceStatus.ACTIVE)
                .plan(PlanType.STARTER)
                .build();

        workspace = workspaceRepository.save(workspace);
        log.info("Created workspace with ID: {}", workspace.getId());

        // Create default roles (Owner and Viewer) for this workspace
        roleService.createDefaultRoles(workspace.getId());

        // Add creator as owner
        Role ownerRole = roleService.findByWorkspaceIdAndName(workspace.getId(), "Owner");
        User owner = userService.findById(ownerId);
        membershipService.addUserToWorkspace(owner, workspace, ownerRole);
        log.info("Added user {} as owner of workspace {}", ownerId, workspace.getId());

        return workspace;
    }

    public Workspace findById(UUID workspaceId) {
        return workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found: " + workspaceId));
    }

    public Workspace createDefaultWorkspace(User user) {
        log.info("Creating default workspace for user: {}", user.getId());

        String workspaceName = user.getName() + "'s Workspace";
        Workspace workspace = Workspace.builder()
                .name(workspaceName)
                .status(WorkspaceStatus.ACTIVE)
                .plan(PlanType.STARTER)
                .build();

        workspace = workspaceRepository.save(workspace);
        log.info("Created default workspace with ID: {}", workspace.getId());

        // Create default role (Owner) for this workspace
        roleService.createDefaultRoles(workspace.getId());

        // Add user as owner
        Role ownerRole = roleService.findByWorkspaceIdAndName(workspace.getId(), "Owner");
        membershipService.addUserToWorkspace(user, workspace, ownerRole);
        log.info("Added user {} as owner of default workspace {}", user.getId(), workspace.getId());

        return workspace;
    }

    public String switchWorkspace(UUID userId, UUID workspaceId) {
        log.info("Attempting to switch workspace '{}' for user '{}'", workspaceId, userId);

        User user = userService.findById(userId);

        List<WorkspaceMembership> memberships = membershipService.findActiveByUserId(user.getId());
        WorkspaceMembership membership = memberships.stream()
                .filter(m -> m.getWorkspace().getId().equals(workspaceId))
                .findFirst()
                .orElseThrow(() -> new AccessDeniedException("User does not have access to this workspace"));

        Set<String> permissions = membership.getRole().getPermissions().stream()
                .map(Permission::getCode)
                .collect(Collectors.toSet());

        log.info("Access granted for workspace '{}' for user '{}'", workspaceId, userId);

        return jwtService.generateAccessToken(
                AccessToken.builder()
                        .userId(user.getId().toString())
                        .email(user.getEmail())
                        .name(user.getName())
                        .workspaceId(workspaceId.toString())
                        .permissions(permissions)
                        .build()
        );
    }
}
