package com.beaver.identity.workspace;

import com.beaver.auth.cookie.AuthCookieService;
import com.beaver.auth.roles.RequiresRole;
import com.beaver.auth.roles.Role;
import com.beaver.identity.auth.dto.AuthResponse;
import com.beaver.identity.common.mapper.GenericMapper;
import com.beaver.identity.workspace.dto.*;
import com.beaver.identity.workspace.entity.Workspace;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/workspaces")
public class WorkspaceController {

    private final WorkspaceService workspaceService;
    private final AuthCookieService cookieService;
    private final GenericMapper mapper;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkspaceDto> createWorkspace(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody CreateWorkspaceRequest request) {

        Workspace workspace = workspaceService.createWorkspace(request, userId);
        return ResponseEntity.ok(mapper.toDto(workspace, WorkspaceDto.class));
    }

    @GetMapping(value = "/current", produces = MediaType.APPLICATION_JSON_VALUE)
    @RequiresRole(Role.READ)
    public ResponseEntity<WorkspaceDto> getWorkspace(
            @RequestHeader("X-Workspace-Id") UUID workspaceId) {
        Workspace workspace = workspaceService.findById(workspaceId);
        return ResponseEntity.ok(mapper.toDto(workspace, WorkspaceDto.class));
    }

    @PatchMapping(value = "/current", produces = MediaType.APPLICATION_JSON_VALUE)
    @RequiresRole(Role.WRITE)
    public ResponseEntity<WorkspaceDto> updateWorkspace(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-Workspace-Id") UUID workspaceId,
            @Valid @RequestBody UpdateWorkspaceRequest updateWorkspaceRequest)
    {
        Workspace workspace = workspaceService.updateWorkspace(userId, workspaceId, updateWorkspaceRequest);
        return ResponseEntity.ok(mapper.toDto(workspace, WorkspaceDto.class));
    }

    @PostMapping("/current/members/invite")
    @RequiresRole(Role.ADMIN)
    public ResponseEntity<String> inviteMember(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-Workspace-Id") UUID workspaceId,
            @Valid @RequestBody InviteMemberRequest request) {

        // TODO: Implementation for inviting members
        return ResponseEntity.ok("Member invited");
    }


    @PostMapping(value = "/switch-workspace", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> switchWorkspace(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody SwitchWorkspaceRequest request
    ) {
        Map<String, String> tokens = workspaceService.switchWorkspace(userId, request.workspaceId());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieService.createAccessTokenCookie(tokens.get("accessToken")).toString())
                .header(HttpHeaders.SET_COOKIE, cookieService.createRefreshTokenCookie(tokens.get("refreshToken")).toString())
                .body(AuthResponse.builder()
                        .success(true)
                        .message("Workspace switched successfully")
                        .userId(userId)
                        .workspaceId(request.workspaceId())
                        .build());
    }
}
