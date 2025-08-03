package com.beaver.identity.workspace;

import com.beaver.identity.workspace.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IWorkspaceRepository extends JpaRepository<Workspace, UUID> {
}