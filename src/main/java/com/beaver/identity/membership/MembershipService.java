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
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Transactional
@Slf4j
@Service
@CacheConfig(cacheNames = "memberships")
public class MembershipService {

    private final IMembershipRepository membershipRepository;
    private final WorkspaceRoleService roleService;
    private final CacheManager cacheManager;

    @Transactional(readOnly = true)
    @Cacheable(key = "'user:' + #userId")
    public List<WorkspaceMembership> findActiveByUserId(UUID userId) {
        return membershipRepository.findByUserIdAndStatus(userId, MembershipStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    @Cacheable(key = "'user:' + #userId + ':workspace:' + #workspaceId")
    public Optional<WorkspaceMembership> findByUserIdAndWorkspaceId(UUID userId, UUID workspaceId) {
        return membershipRepository.findByUserIdAndWorkspaceIdAndStatus(userId, workspaceId, MembershipStatus.ACTIVE);
    }

    @Caching(evict = {
            @CacheEvict(key = "'user:' + #user.id"),
            @CacheEvict(key = "'user:' + #user.id + ':workspace:' + #workspace.id")
    })
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

        return membershipRepository.save(membership);
    }

    public void evictCache(UUID userId) {
        log.debug("Evicting all membership cache entries for user: {}", userId);

        var cache = cacheManager.getCache("memberships");
        if (cache != null) {
            String userCacheKey = "user:" + userId;
            cache.evict(userCacheKey);
            log.debug("Evicted cache key: {}", userCacheKey);

            List<WorkspaceMembership> memberships = membershipRepository.findByUserIdAndStatus(userId, MembershipStatus.ACTIVE);
            memberships.forEach(membership -> {
                String userWorkspaceCacheKey = "user:" + userId + ":workspace:" + membership.getWorkspace().getId();
                cache.evict(userWorkspaceCacheKey);
                log.debug("Evicted cache key: {}", userWorkspaceCacheKey);
            });
        }
    }
}
