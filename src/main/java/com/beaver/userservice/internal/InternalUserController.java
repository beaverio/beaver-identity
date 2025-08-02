package com.beaver.userservice.internal;

import com.beaver.userservice.auth.AuthService;
import com.beaver.userservice.auth.dto.UserWithWorkspacesDto;
import com.beaver.userservice.common.exception.InvalidUserDataException;
import com.beaver.userservice.common.exception.UserAlreadyExistsException;
import com.beaver.userservice.common.exception.UserNotFoundException;
import com.beaver.userservice.internal.dto.CreateUserRequest;
import com.beaver.userservice.internal.dto.CredentialsRequest;
import com.beaver.userservice.internal.dto.UpdateEmail;
import com.beaver.userservice.internal.dto.UpdatePassword;
import com.beaver.userservice.membership.MembershipService;
import com.beaver.userservice.membership.dto.WorkspaceMembershipDto;
import com.beaver.userservice.membership.entity.WorkspaceMembership;
import com.beaver.userservice.user.entity.User;
import com.beaver.userservice.user.UserService;
import com.beaver.userservice.user.dto.UserDto;
import com.beaver.userservice.workspace.WorkspaceService;
import com.beaver.userservice.workspace.dto.CreateWorkspaceRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserService userService;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    private final WorkspaceService workspaceService;
    private final MembershipService membershipService;

    @PostMapping("/validate-credentials")
    public ResponseEntity<UserDto> validateCredentials(@Valid @RequestBody CredentialsRequest request) {
        User user = userService.findByEmail(request.email())
                .orElseThrow(() -> new UserNotFoundException("Email not found"));

        if (user.isActive() && passwordEncoder.matches(request.password(), user.getPassword())) {
            return ResponseEntity.ok(UserDto.fromEntity(user));
        }

        throw new InvalidUserDataException("Invalid credentials");
    }

    @PostMapping("/validate-credentials-with-workspaces")
    public ResponseEntity<UserWithWorkspacesDto> validateCredentialsWithWorkspaces(
            @Valid @RequestBody CredentialsRequest request) {

        UserWithWorkspacesDto result = authService.validateCredentialsWithWorkspaces(
                request.email(), request.password());

        return ResponseEntity.ok(result);
    }

    @PostMapping("/validate-workspace-access")
    public ResponseEntity<WorkspaceMembershipDto> validateWorkspaceAccess(
            @RequestParam UUID userId,
            @RequestParam UUID workspaceId) {

        WorkspaceMembership membership = authService.validateUserWorkspaceAccess(userId, workspaceId);
        return ResponseEntity.ok(WorkspaceMembershipDto.fromEntity(membership));
    }

    @PostMapping("/users")
    public ResponseEntity<Void> createUser(@Valid @RequestBody CreateUserRequest request) {
        if (userService.findByEmail(request.email()).isPresent()) {
            throw new UserAlreadyExistsException(request.email());
        }

        try {
            // Create the user first
            User user = User.builder()
                    .email(request.email())
                    .password(passwordEncoder.encode(request.password()))
                    .name(request.name())
                    .isActive(true)
                    .build();

            User savedUser = userService.saveUser(user);

            // Create default workspace with format "User's Budget"
            String workspaceName = savedUser.getName() + "'s Budget";
            CreateWorkspaceRequest workspaceRequest = new CreateWorkspaceRequest(workspaceName);

            workspaceService.createWorkspace(workspaceRequest, savedUser.getId());

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            throw new InvalidUserDataException("Failed to create user: " + e.getMessage());
        }
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable UUID userId) {
        User user = userService.findById(userId);
        return ResponseEntity.ok(UserDto.fromEntity(user));
    }

    @PatchMapping("/users/{userId}/email")
    public ResponseEntity<UserDto> updateEmail(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateEmail updateEmail)
    {
        User user = userService.updateEmail(userId, updateEmail);
        return ResponseEntity.ok(UserDto.fromEntity(user));
    }

    @PatchMapping("/users/{userId}/password")
    public ResponseEntity<Void> updatePassword(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdatePassword updatePassword)
    {
        userService.updatePassword(userId, updatePassword);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/{userId}/workspaces")
    public ResponseEntity<UserWithWorkspacesDto> getUserWorkspaces(@PathVariable UUID userId) {
        User user = userService.findById(userId);
        List<WorkspaceMembership> memberships = membershipService.findActiveByUserId(userId);

        UserWithWorkspacesDto result = UserWithWorkspacesDto.fromUserAndMemberships(user, memberships);
        return ResponseEntity.ok(result);
    }
}
