package com.beaver.identity.user;

import com.beaver.auth.cookie.AuthCookieService;
import com.beaver.auth.jwt.JwtService;
import com.beaver.auth.permissions.Permission;
import com.beaver.auth.permissions.RequiresPermission;
import com.beaver.identity.auth.dto.AuthResponse;
import com.beaver.identity.internal.dto.UpdateEmail;
import com.beaver.identity.internal.dto.UpdatePassword;
import com.beaver.identity.membership.MembershipService;
import com.beaver.identity.membership.entity.WorkspaceMembership;
import com.beaver.identity.user.dto.UpdateSelf;
import com.beaver.identity.user.dto.UserDto;
import com.beaver.identity.user.entity.User;
import com.beaver.identity.workspace.dto.WorkspaceListDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/self")
public class UserController {

    private final UserService userService;
    private final MembershipService membershipService;
    private final JwtService jwtService;
    private final AuthCookieService cookieService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @RequiresPermission(Permission.USER_READ)
    public ResponseEntity<UserDto> getSelf(@RequestHeader("X-User-Id") UUID id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(UserDto.fromEntity(user));
    }

    @PatchMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @RequiresPermission(Permission.USER_WRITE)
    public ResponseEntity<UserDto> updateSelf(
            @RequestHeader("X-User-Id") UUID id,
            @Valid @RequestBody UpdateSelf updateSelf)
    {
        User user = userService.updateSelf(id, updateSelf);
        return ResponseEntity.ok(UserDto.fromEntity(user));
    }

    @DeleteMapping()
    @RequiresPermission(Permission.USER_WRITE)
    public ResponseEntity<Void> deleteSelf(@RequestHeader("X-User-Id") UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/workspaces", produces = MediaType.APPLICATION_JSON_VALUE)
    @RequiresPermission(value={ Permission.USER_READ, Permission.WORKSPACE_READ }, requireAll = true)
    public ResponseEntity<List<WorkspaceListDto>> getUserWorkspaces(@RequestHeader("X-User-Id") UUID userId) {
        List<WorkspaceMembership> memberships = membershipService.findActiveByUserId(userId);
        return ResponseEntity.ok(WorkspaceListDto.fromMemberships(memberships));
    }

    @PatchMapping("/update-email")
    @RequiresPermission(Permission.USER_WRITE)
    public ResponseEntity<AuthResponse> updateEmail(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-Workspace-Id") UUID workspaceId,
            @Valid @RequestBody UpdateEmail updateEmail)
    {
        User user = userService.updateEmail(userId, updateEmail);

        // Generate new access token with updated email
        List<WorkspaceMembership> memberships = membershipService.findActiveByUserId(userId);
        WorkspaceMembership currentMembership = memberships.stream()
                .filter(m -> m.getWorkspace().getId().equals(workspaceId))
                .findFirst()
                .orElse(memberships.getFirst());

        Set<String> permissions = currentMembership.getRole().getPermissions().stream()
                .map(com.beaver.identity.permission.entity.Permission::getCode)
                .collect(Collectors.toSet());

        String newAccessToken = jwtService.generateAccessToken(
                user.getId().toString(),
                user.getEmail(),
                user.getName(),
                workspaceId.toString(),
                permissions
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieService.createAccessTokenCookie(newAccessToken).toString())
                .body(AuthResponse.builder()
                        .success(true)
                        .message("Email updated successfully")
                        .userId(user.getId())
                        .workspaceId(workspaceId)
                        .build());
    }

    @PatchMapping("/update-password")
    @RequiresPermission(Permission.USER_WRITE)
    public ResponseEntity<Void> updatePassword(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody UpdatePassword updatePassword)
    {
        userService.updatePassword(userId, updatePassword);
        return ResponseEntity.noContent().build();
    }
}
