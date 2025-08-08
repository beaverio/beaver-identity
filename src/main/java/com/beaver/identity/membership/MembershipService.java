package com.beaver.identity.membership;

import com.beaver.auth.roles.Role;
import com.beaver.identity.membership.enums.MembershipStatus;
import com.beaver.identity.membership.entity.WorkspaceMembership;
import com.beaver.identity.role.entity.WorkspaceRole;
import com.beaver.identity.role.service.WorkspaceRoleService;
import com.beaver.identity.user.entity.User;
import com.beaver.identity.workspace.entity.Workspace;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Transactional
@Slf4j
@Service
public class MembershipService {

    private final IMembershipRepository membershipRepository;
    private final WorkspaceRoleService roleService;

    public List<WorkspaceMembership> findActiveByUserId(UUID userId) {
        return membershipRepository.findByUserIdAndStatus(userId, MembershipStatus.ACTIVE);
    }

    public WorkspaceMembership addUserToWorkspace(User user, Workspace workspace, Role roleType) {
        log.info("Adding user {} to workspace {} with role {}", user.getId(), workspace.getId(), roleType);

        WorkspaceRole role = roleService.findByWorkspaceIdAndRoleType(workspace.getId(), roleType)
                .orElseThrow(() -> new IllegalStateException(
                        "Role " + roleType + " not found for workspace " + workspace.getId() +
                        ". This should not happen as all roles should be created during workspace setup."));

        WorkspaceMembership membership = WorkspaceMembership.builder()
                .user(user)
                .workspace(workspace)
                .role(role)
                .status(MembershipStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();

        WorkspaceMembership saved = membershipRepository.save(membership);
        evictMembershipCache(user.getId(), workspace.getId());

        return saved;
    }

    @CacheEvict(value = "memberships", key = "'user:' + #userId + ':workspace:' + #workspaceId")
    public void evictMembershipCache(UUID userId, UUID workspaceId) {
        log.debug("Evicting membership cache for user {} and workspace {}", userId, workspaceId);
    }
}
