package com.beaver.identity.workspace;

import com.beaver.auth.cookie.AuthCookieService;
import com.beaver.auth.roles.RequiresRole;
import com.beaver.auth.roles.Role;
import com.beaver.identity.auth.dto.AuthResponse;
import com.beaver.identity.workspace.dto.CreateWorkspaceRequest;
import com.beaver.identity.workspace.dto.InviteMemberRequest;
import com.beaver.identity.workspace.dto.SwitchWorkspaceRequest;
import com.beaver.identity.workspace.dto.WorkspaceDto;
import com.beaver.identity.workspace.entity.Workspace;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/workspaces")
public class WorkspaceController {

    private final WorkspaceService workspaceService;
    private final AuthCookieService cookieService;

    @PostMapping
    public ResponseEntity<WorkspaceDto> createWorkspace(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody CreateWorkspaceRequest request) {

        Workspace workspace = workspaceService.createWorkspace(request, userId);
        return ResponseEntity.ok(WorkspaceDto.fromEntity(workspace));
    }

    @GetMapping("/current")
    @RequiresRole(Role.READ)
    public ResponseEntity<WorkspaceDto> getWorkspace(
            @RequestHeader("X-Workspace-Id") UUID workspaceId) {
        Workspace workspace = workspaceService.findById(workspaceId);
        return ResponseEntity.ok(WorkspaceDto.fromEntity(workspace));
    }

    @PostMapping("/current/members/invite")
    @RequiresRole(Role.ADMIN)
    public ResponseEntity<String> inviteMember(
            @RequestHeader("X-User-Id") UUID inviterId,
            @RequestHeader("X-Workspace-Id") UUID workspaceId,
            @Valid @RequestBody InviteMemberRequest request) {

        // TODO: Implementation for inviting members
        return ResponseEntity.ok("Member invited");
    }


    @PostMapping("/switch-workspace")
    public ResponseEntity<AuthResponse> switchWorkspace(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody SwitchWorkspaceRequest request
    ) {
        String newAccessToken = workspaceService.switchWorkspace(userId, request.workspaceId());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieService.createAccessTokenCookie(newAccessToken).toString())
                .body(AuthResponse.builder()
                        .success(true)
                        .message("Workspace switched successfully")
                        .userId(userId)
                        .workspaceId(request.workspaceId())
                        .build());
    }
}
