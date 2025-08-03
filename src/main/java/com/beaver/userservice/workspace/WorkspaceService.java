package com.beaver.userservice.workspace;

import com.beaver.userservice.membership.MembershipService;
import com.beaver.userservice.permission.RoleService;
import com.beaver.userservice.permission.entity.Role;
import com.beaver.userservice.user.UserService;
import com.beaver.userservice.user.entity.User;
import com.beaver.userservice.workspace.dto.CreateWorkspaceRequest;
import com.beaver.userservice.workspace.enums.PlanType;
import com.beaver.userservice.workspace.entity.Workspace;
import com.beaver.userservice.workspace.enums.WorkspaceStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class WorkspaceService {

    private final IWorkspaceRepository workspaceRepository;
    private final RoleService roleService;
    private final MembershipService membershipService;
    private final UserService userService;

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
}
