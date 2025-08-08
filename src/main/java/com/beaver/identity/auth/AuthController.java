package com.beaver.identity.auth;

import com.beaver.auth.jwt.AccessToken;
import com.beaver.auth.jwt.JwtService;
import com.beaver.auth.cookie.AuthCookieService;
import com.beaver.auth.exceptions.AuthenticationFailedException;
import com.beaver.auth.exceptions.InvalidRefreshTokenException;
import com.beaver.identity.auth.dto.AuthResponse;
import com.beaver.identity.auth.dto.LoginRequest;
import com.beaver.identity.auth.dto.SignupRequest;
import com.beaver.identity.user.UserService;
import com.beaver.identity.user.entity.User;
import com.beaver.identity.workspace.WorkspaceService;
import com.beaver.identity.workspace.entity.Workspace;
import com.beaver.identity.membership.MembershipService;
import com.beaver.identity.membership.entity.WorkspaceMembership;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final WorkspaceService workspaceService;
    private final MembershipService membershipService;
    private final JwtService jwtService;
    private final AuthCookieService cookieService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        Optional<User> userOpt = userService.findByEmail(request.email());
        if (userOpt.isEmpty()) {
            throw new AuthenticationFailedException("Email not found");
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new AuthenticationFailedException("Email or password incorrect");
        }

        List<WorkspaceMembership> memberships = membershipService.findActiveByUserId(user.getId());
        if (memberships.isEmpty()) {
            throw new AuthenticationFailedException("User has no active workspaces");
        }

        // TODO: Login to a default workspace, optionlly allow workspaceId within login request
        WorkspaceMembership membership = memberships.getFirst();

        String accessToken = jwtService.generateAccessToken(
                AccessToken.builder()
                        .userId(user.getId().toString())
                        .email(user.getEmail())
                        .name(user.getName())
                        .workspaceId(membership.getWorkspace().getId().toString())
                        .role(membership.getRole().toString())
                        .build()
        );

        String refreshToken = jwtService.generateRefreshToken(user.getId().toString());

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, cookieService.createAccessTokenCookie(accessToken).toString())
            .header(HttpHeaders.SET_COOKIE, cookieService.createRefreshTokenCookie(refreshToken).toString())
            .body(AuthResponse.builder()
                    .success(true)
                    .message("Login successful")
                    .userId(user.getId())
                    .workspaceId(membership.getWorkspace().getId())
                    .build()
            );
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        Optional<User> existingUser = userService.findByEmail(request.email());
        if (existingUser.isPresent()) {
            throw new AuthenticationFailedException("An account already exists with that email, do you want to login?");
        }

        User user = userService.createUser(request.email(), request.password(), request.name());
        WorkspaceMembership membership = workspaceService.createDefaultWorkspace(user);

        String accessToken = jwtService.generateAccessToken(
                AccessToken.builder()
                        .userId(user.getId().toString())
                        .email(user.getEmail())
                        .name(user.getName())
                        .workspaceId(membership.getWorkspace().getId().toString())
                        .role(membership.getRole().toString())
                        .build()
        );

        String refreshToken = jwtService.generateRefreshToken(user.getId().toString());

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, cookieService.createAccessTokenCookie(accessToken).toString())
            .header(HttpHeaders.SET_COOKIE, cookieService.createRefreshTokenCookie(refreshToken).toString())
            .body(AuthResponse.builder()
                    .success(true)
                    .message("Signup successful")
                    .userId(user.getId())
                    .workspaceId(membership.getWorkspace().getId())
                    .build()
            );
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest request) {
        String refreshToken = cookieService.extractRefreshToken(request);
        if (refreshToken == null) {
            throw new InvalidRefreshTokenException("Refresh token cookie not found");
        }

        jwtService.validateRefreshToken(refreshToken).block();
        String userId = jwtService.extractUserIdFromToken(refreshToken).block();

        assert userId != null;
        User user = userService.findById(UUID.fromString(userId));

        List<WorkspaceMembership> memberships = membershipService.findActiveByUserId(user.getId());
        if (memberships.isEmpty()) {
            throw new AuthenticationFailedException("User has no active workspaces");
        }

        // TODO: Add workspaceId to refresh token to allow refresh of current workspace
        WorkspaceMembership membership = memberships.getFirst();

        String newAccessToken = jwtService.generateAccessToken(
                AccessToken.builder()
                        .userId(user.getId().toString())
                        .email(user.getEmail())
                        .name(user.getName())
                        .workspaceId(membership.getWorkspace().getId().toString())
                        .role(membership.getRole().toString())
                        .build()
        );

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, cookieService.createAccessTokenCookie(newAccessToken).toString())
            .body(AuthResponse.builder()
                    .success(true)
                    .message("Token refresh successful")
                    .userId(user.getId())
                    .workspaceId(membership.getWorkspace().getId())
                    .build()
            );
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout() {
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, cookieService.clearAccessTokenCookie().toString())
            .header(HttpHeaders.SET_COOKIE, cookieService.clearRefreshTokenCookie().toString())
            .body(AuthResponse.builder()
                .success(true)
                .message("Logout successful")
                .build());
    }
}
