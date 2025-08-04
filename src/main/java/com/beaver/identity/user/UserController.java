package com.beaver.identity.user;

import com.beaver.auth.cookie.AuthCookieService;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/self")
public class UserController {

    private final UserService userService;
    private final MembershipService membershipService;
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
        String newAccessToken = userService.updateEmailWithNewToken(userId, workspaceId, updateEmail);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieService.createAccessTokenCookie(newAccessToken).toString())
                .body(AuthResponse.builder()
                        .success(true)
                        .message("Email updated successfully")
                        .userId(userId)
                        .build());
    }

    @PatchMapping("/update-password")
    @RequiresPermission(Permission.USER_WRITE)
    public ResponseEntity<AuthResponse> updatePassword(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody UpdatePassword updatePassword)
    {
        userService.updatePassword(userId, updatePassword);

        return ResponseEntity.ok()
                .body(AuthResponse.builder()
                        .success(true)
                        .message("Password updated successfully")
                        .userId(userId)
                        .build());
    }
}
