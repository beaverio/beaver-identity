package com.beaver.identity.role.service;

import com.beaver.auth.roles.Role;
import com.beaver.identity.role.entity.WorkspaceRole;
import com.beaver.identity.role.repository.IWorkspaceRoleRepository;
import com.beaver.identity.workspace.entity.Workspace;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Transactional
@Slf4j
@Service
public class WorkspaceRoleService {

    private final IWorkspaceRoleRepository roleRepository;

    public void createDefaultRoles(UUID workspaceId, Workspace workspace) {
        log.info("Creating default RBAC roles for workspace: {}", workspaceId);

        Set<Role> existingRoles = roleRepository.findByWorkspaceId(workspaceId)
                .stream()
                .map(WorkspaceRole::getRoleType)
                .collect(Collectors.toSet());

        List<WorkspaceRole> rolesToCreate = Arrays.stream(Role.values())
                .filter(roleType -> !existingRoles.contains(roleType))
                .map(roleType -> WorkspaceRole.builder()
                        .workspace(workspace)
                        .roleType(roleType)
                        .build())
                .collect(Collectors.toList());

        if (!rolesToCreate.isEmpty()) {
            roleRepository.saveAll(rolesToCreate);
            log.debug("Created {} roles for workspace {}: {}",
                     rolesToCreate.size(), workspaceId,
                     rolesToCreate.stream().map(WorkspaceRole::getRoleType).collect(Collectors.toList()));
        } else {
            log.debug("All roles already exist for workspace {}", workspaceId);
        }
    }

    @Transactional(readOnly = true)
    public Optional<WorkspaceRole> findByWorkspaceIdAndRoleType(UUID workspaceId, Role roleType) {
        return roleRepository.findByWorkspaceIdAndRoleType(workspaceId, roleType);
    }
}
