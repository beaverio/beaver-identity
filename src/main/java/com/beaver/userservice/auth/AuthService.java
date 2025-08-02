package com.beaver.userservice.auth;

import com.beaver.userservice.auth.dto.UserWithWorkspacesDto;
import com.beaver.userservice.common.exception.InvalidUserDataException;
import com.beaver.userservice.common.exception.UserNotFoundException;
import com.beaver.userservice.membership.MembershipService;
import com.beaver.userservice.membership.entity.WorkspaceMembership;
import com.beaver.userservice.user.UserService;
import com.beaver.userservice.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserService userService;
    private final MembershipService membershipService;
    private final PasswordEncoder passwordEncoder;

    public UserWithWorkspacesDto validateCredentialsWithWorkspaces(String email, String password) {
        log.debug("Validating credentials for email: {}", email);

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Email not found"));

        if (!user.isActive() || !passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidUserDataException("Invalid credentials");
        }

        // Get all user's workspace memberships with permissions
        List<WorkspaceMembership> memberships = membershipService.findActiveByUserId(user.getId());
        log.debug("Found {} active memberships for user: {}", memberships.size(), user.getId());

        return UserWithWorkspacesDto.fromUserAndMemberships(user, memberships);
    }

    public WorkspaceMembership validateUserWorkspaceAccess(UUID userId, UUID workspaceId) {
        return membershipService.findByUserIdAndWorkspaceId(userId, workspaceId)
                .orElseThrow(() -> new InvalidUserDataException("No access to workspace"));
    }
}
