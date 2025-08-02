package com.beaver.userservice.membership;

import com.beaver.userservice.membership.enums.MembershipStatus;
import com.beaver.userservice.membership.entity.WorkspaceMembership;
import com.beaver.userservice.permission.entity.Role;
import com.beaver.userservice.user.entity.User;
import com.beaver.userservice.workspace.entity.Workspace;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MembershipService {

    private final IMembershipRepository membershipRepository;

    @Cacheable(value = "memberships", key = "'user:' + #userId")
    public List<WorkspaceMembership> findActiveByUserId(UUID userId) {
        return membershipRepository.findByUserIdAndStatus(userId, MembershipStatus.ACTIVE);
    }

    @Cacheable(value = "memberships", key = "'user:' + #userId + ':workspace:' + #workspaceId")
    public Optional<WorkspaceMembership> findByUserIdAndWorkspaceId(UUID userId, UUID workspaceId) {
        return membershipRepository.findByUserIdAndWorkspaceIdAndStatus(
                userId, workspaceId, MembershipStatus.ACTIVE);
    }

    public Set<String> getUserPermissions(UUID userId, UUID workspaceId) {
        return findByUserIdAndWorkspaceId(userId, workspaceId)
                .map(WorkspaceMembership::getAllPermissionCodes)
                .orElse(Set.of());
    }

    public List<WorkspaceMembership> findMembersByWorkspaceId(UUID workspaceId) {
        return membershipRepository.findByWorkspaceIdAndStatus(workspaceId, MembershipStatus.ACTIVE);
    }

    public WorkspaceMembership addUserToWorkspace(User user, Workspace workspace, Role role) {
        log.info("Adding user {} to workspace {} with role {}", user.getId(), workspace.getId(), role.getName());

        WorkspaceMembership membership = WorkspaceMembership.builder()
                .user(user)
                .workspace(workspace)
                .role(role)
                .status(MembershipStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();

        WorkspaceMembership saved = membershipRepository.save(membership);

        // Clear cache for this user
        evictMembershipCache(user.getId(), workspace.getId());

        return saved;
    }

    @CacheEvict(value = "memberships", key = "'user:' + #userId")
    public void evictUserMembershipsCache(UUID userId) {
        log.debug("Evicting memberships cache for user: {}", userId);
    }

    @CacheEvict(value = "memberships", key = "'user:' + #userId + ':workspace:' + #workspaceId")
    public void evictMembershipCache(UUID userId, UUID workspaceId) {
        log.debug("Evicting membership cache for user {} and workspace {}", userId, workspaceId);
    }
}
