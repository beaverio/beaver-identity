package com.beaver.identity.permission;

import com.beaver.identity.permission.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.UUID;

@Repository
public interface IPermissionRepository  extends JpaRepository<Permission, UUID> {
    Set<Permission> findByCodeIn(Set<String> strings);
}
