package com.beaver.userservice.permission;

import com.beaver.auth.Permission;
import com.beaver.userservice.permission.entity.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RoleService {

    private final IRoleRepository roleRepository;
    private final IPermissionRepository permissionRepository;

    public void createDefaultRoles(UUID workspaceId) {
        log.info("Creating default roles for workspace: {}", workspaceId);

        Role owner = createRole(workspaceId, "Owner", "Full access to all workspace resources", true);
        Set<com.beaver.userservice.permission.entity.Permission> allPermissions = Set.copyOf(permissionRepository.findAll());
        owner.setPermissions(allPermissions);
        roleRepository.save(owner);
        log.info("Created Owner role with {} permissions", allPermissions.size());

        Role viewer = createRole(workspaceId, "Viewer", "Read-only access to workspace data", true);
        Set<com.beaver.userservice.permission.entity.Permission> viewerPermissions = permissionRepository.findByCodeIn(
                Stream.of(Permission.TRANSACTION_READ, Permission.BUDGET_READ, Permission.REPORT_READ)
                        .map(Permission::getValue)
                        .collect(Collectors.toSet())
        );
        viewer.setPermissions(viewerPermissions);
        roleRepository.save(viewer);
        log.info("Created Viewer role with {} permissions", viewerPermissions.size());
    }

    private Role createRole(UUID workspaceId, String name, String description, boolean isSystemRole) {
        return Role.builder()
                .workspaceId(workspaceId)
                .name(name)
                .description(description)
                .isSystemRole(isSystemRole)
                .build();
    }

    public Role findByWorkspaceIdAndName(UUID workspaceId, String name) {
        return roleRepository.findByWorkspaceIdAndName(workspaceId, name)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + name));
    }
}
