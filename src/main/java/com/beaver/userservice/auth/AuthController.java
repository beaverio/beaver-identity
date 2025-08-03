package com.beaver.userservice.auth;

import com.beaver.auth.jwt.JwtService;
import com.beaver.auth.exceptions.AuthenticationFailedException;
import com.beaver.userservice.auth.dto.AuthResponse;
import com.beaver.userservice.auth.dto.LoginRequest;
import com.beaver.userservice.auth.dto.SignupRequest;
import com.beaver.userservice.user.UserService;
import com.beaver.userservice.user.entity.User;
import com.beaver.userservice.workspace.WorkspaceService;
import com.beaver.userservice.workspace.entity.Workspace;
import com.beaver.userservice.membership.MembershipService;
import com.beaver.userservice.membership.entity.WorkspaceMembership;
import com.beaver.userservice.permission.entity.Permission;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final WorkspaceService workspaceService;
    private final MembershipService membershipService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            // Validate user credentials
            Optional<User> userOpt = userService.findByEmail(request.email());
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(401).body(
                    AuthResponse.builder()
                        .success(false)
                        .message("Email not found")
                        .build()
                );
            }

            User user = userOpt.get();
            if (!passwordEncoder.matches(request.password(), user.getPassword())) {
                return ResponseEntity.status(401).body(
                    AuthResponse.builder()
                        .success(false)
                        .message("Invalid credentials")
                        .build()
                );
            }

            // Get user's workspace memberships
            List<WorkspaceMembership> memberships = membershipService.findActiveByUserId(user.getId());
            if (memberships.isEmpty()) {
                return ResponseEntity.status(401).body(
                    AuthResponse.builder()
                        .success(false)
                        .message("User has no active workspaces")
                        .build()
                );
            }

            // TODO: Allow User to set a default workspace
            WorkspaceMembership primaryMembership = memberships.get(0);
            Set<String> permissions = primaryMembership.getRole().getPermissions().stream()
                .map(Permission::getCode)
                .collect(Collectors.toSet());

            // Generate JWT tokens
            String accessToken = jwtService.generateAccessToken(
                user.getId().toString(),
                user.getEmail(),
                user.getName(),
                primaryMembership.getWorkspace().getId().toString(),
                permissions
            );

            String refreshToken = jwtService.generateRefreshToken(user.getId().toString());

            return ResponseEntity.ok(AuthResponse.builder()
                .success(true)
                .message("Login successful")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(AuthResponse.UserInfo.builder()
                    .id(user.getId().toString())
                    .email(user.getEmail())
                    .name(user.getName())
                    .build())
                .workspace(AuthResponse.WorkspaceInfo.builder()
                    .id(primaryMembership.getWorkspace().getId().toString())
                    .name(primaryMembership.getWorkspace().getName())
                    .build())
                .build());

        } catch (Exception e) {
            log.error("Login failed for email: {}", request.email(), e);
            return ResponseEntity.status(401).body(
                AuthResponse.builder()
                    .success(false)
                    .message("Authentication failed")
                    .build()
            );
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        try {
            // Check if user already exists
            Optional<User> existingUser = userService.findByEmail(request.email());
            if (existingUser.isPresent()) {
                return ResponseEntity.status(409).body(
                    AuthResponse.builder()
                        .success(false)
                        .message("Account already exists with that email, do you want to login?")
                        .build()
                );
            }

            // Create user and default workspace
            User user = userService.createUser(request.email(), request.password(), request.name());
            Workspace workspace = workspaceService.createDefaultWorkspace(user);

            // Get the membership that was created
            Optional<WorkspaceMembership> membershipOpt = membershipService.findByUserIdAndWorkspaceId(user.getId(), workspace.getId());
            if (membershipOpt.isEmpty()) {
                return ResponseEntity.status(500).body(
                    AuthResponse.builder()
                        .success(false)
                        .message("Failed to find workspace membership after creation")
                        .build()
                );
            }

            WorkspaceMembership membership = membershipOpt.get();
            Set<String> permissions = membership.getRole().getPermissions().stream()
                .map(Permission::getCode)
                .collect(Collectors.toSet());

            // Generate JWT tokens
            String accessToken = jwtService.generateAccessToken(
                user.getId().toString(),
                user.getEmail(),
                user.getName(),
                workspace.getId().toString(),
                permissions
            );

            String refreshToken = jwtService.generateRefreshToken(user.getId().toString());

            return ResponseEntity.ok(AuthResponse.builder()
                .success(true)
                .message("Signup successful")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(AuthResponse.UserInfo.builder()
                    .id(user.getId().toString())
                    .email(user.getEmail())
                    .name(user.getName())
                    .build())
                .workspace(AuthResponse.WorkspaceInfo.builder()
                    .id(workspace.getId().toString())
                    .name(workspace.getName())
                    .build())
                .build());

        } catch (Exception e) {
            log.error("Signup failed for email: {}", request.email(), e);
            return ResponseEntity.status(400).body(
                AuthResponse.builder()
                    .success(false)
                    .message("Signup failed: " + e.getMessage())
                    .build()
            );
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestHeader("Authorization") String authHeader) {
        String refreshToken = extractTokenFromHeader(authHeader);

        jwtService.validateRefreshToken(refreshToken).block();
        String userId = jwtService.extractUserIdFromToken(refreshToken).block();

        assert userId != null;
        User user = userService.findById(UUID.fromString(userId));

        // TODO: Refresh for workspace in access_token/refresh_token, critical
        List<WorkspaceMembership> memberships = membershipService.findActiveByUserId(user.getId());
        if (memberships.isEmpty()) {
            throw new AuthenticationFailedException("No workspace access");
        }

        WorkspaceMembership primaryMembership = memberships.getFirst();
        Set<String> permissions = primaryMembership.getRole().getPermissions().stream()
            .map(Permission::getCode)
            .collect(Collectors.toSet());

        String newAccessToken = jwtService.generateAccessToken(
            user.getId().toString(),
            user.getEmail(),
            user.getName(),
            primaryMembership.getWorkspace().getId().toString(),
            permissions
        );

        return ResponseEntity.ok(AuthResponse.builder()
            .success(true)
            .message("Token refreshed successfully")
            .accessToken(newAccessToken)
            .refreshToken(refreshToken)
            .build());
    }

    private String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new IllegalArgumentException("Invalid authorization header");
    }
}
