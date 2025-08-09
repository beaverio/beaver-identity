package com.beaver.identity.role.repository;

import com.beaver.auth.roles.Role;
import com.beaver.identity.role.entity.WorkspaceRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IWorkspaceRoleRepository extends JpaRepository<WorkspaceRole, UUID> {

    Optional<WorkspaceRole> findByWorkspaceIdAndRoleType(UUID workspaceId, Role roleType);
    List<WorkspaceRole> findByWorkspaceId(UUID workspaceId);
}
