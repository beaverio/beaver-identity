package com.beaver.identity.workspace;

import com.beaver.auth.permissions.RequiresPermission;
import com.beaver.auth.permissions.Permission;
import com.beaver.identity.workspace.dto.CreateWorkspaceRequest;
import com.beaver.identity.workspace.dto.InviteMemberRequest;
import com.beaver.identity.workspace.dto.WorkspaceDto;
import com.beaver.identity.workspace.entity.Workspace;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/workspaces")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @PostMapping
    public ResponseEntity<WorkspaceDto> createWorkspace(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody CreateWorkspaceRequest request) {

        Workspace workspace = workspaceService.createWorkspace(request, userId);
        return ResponseEntity.ok(WorkspaceDto.fromEntity(workspace));
    }

    @GetMapping("/current")
    @RequiresPermission(Permission.WORKSPACE_READ)
    public ResponseEntity<WorkspaceDto> getWorkspace(
            @RequestHeader("X-Workspace-Id") UUID workspaceId) {
        Workspace workspace = workspaceService.findById(workspaceId);
        return ResponseEntity.ok(WorkspaceDto.fromEntity(workspace));
    }

    @PostMapping("/current/members/invite")
    @RequiresPermission(Permission.WORKSPACE_OWNER)
    public ResponseEntity<String> inviteMember(
            @RequestHeader("X-User-Id") UUID inviterId,
            @RequestHeader("X-Workspace-Id") UUID workspaceId,
            @Valid @RequestBody InviteMemberRequest request) {

        // TODO: Implementation for inviting members

        return ResponseEntity.ok("Member invited");
    }
}
