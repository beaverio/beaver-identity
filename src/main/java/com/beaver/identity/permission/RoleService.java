package com.beaver.identity.permission;

import com.beaver.auth.permissions.Permission;
import com.beaver.identity.permission.entity.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
        Set<com.beaver.identity.permission.entity.Permission> allPermissions = permissionRepository.findAll()
                .stream()
                .filter(permission -> !Permission.DENY_ALL.getValue().equals(permission.getCode()))
                .collect(Collectors.toSet());
        owner.setPermissions(allPermissions);
        roleRepository.save(owner);
        log.info("Created Owner role with {} permissions", allPermissions.size());
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
