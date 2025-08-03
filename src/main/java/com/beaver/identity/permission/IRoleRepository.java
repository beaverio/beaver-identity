package com.beaver.identity.permission;

import com.beaver.identity.permission.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IRoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByWorkspaceIdAndName(UUID workspaceId, String name);
}
