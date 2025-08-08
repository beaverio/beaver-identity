package com.beaver.identity.membership;

import com.beaver.identity.membership.entity.WorkspaceMembership;
import com.beaver.identity.membership.enums.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IMembershipRepository extends JpaRepository<WorkspaceMembership, UUID> {
    List<WorkspaceMembership> findByUserIdAndStatus(UUID userId, MembershipStatus status);
    Optional<WorkspaceMembership> findByUserIdAndWorkspaceIdAndStatus(UUID userId, UUID workspaceId, MembershipStatus status);
}
