package com.beaver.userservice.membership;

import com.beaver.userservice.membership.entity.WorkspaceMembership;
import com.beaver.userservice.membership.enums.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IMembershipRepository extends JpaRepository<WorkspaceMembership, UUID> {
    List<WorkspaceMembership> findByUserIdAndStatus(UUID userId, MembershipStatus status);
    Optional<WorkspaceMembership> findByUserIdAndWorkspaceIdAndStatus(UUID userId, UUID workspaceId, MembershipStatus status);
    List<WorkspaceMembership> findByWorkspaceIdAndStatus(UUID workspaceId, MembershipStatus status);
}
