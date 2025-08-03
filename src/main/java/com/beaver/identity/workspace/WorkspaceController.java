package com.beaver.identity.workspace;

import com.beaver.auth.exceptions.AccessDeniedException;
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

    @GetMapping("/{id}")
    public ResponseEntity<WorkspaceDto> getWorkspace(@PathVariable UUID id) {
        Workspace workspace = workspaceService.findById(id);
        return ResponseEntity.ok(WorkspaceDto.fromEntity(workspace));
    }

    @PostMapping("/{id}/members/invite")
    @RequiresPermission(Permission.WORKSPACE_MEMBERS)
    public ResponseEntity<String> inviteMember(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") UUID inviterId,
            @RequestHeader("X-Workspace-Id") UUID currentWorkspaceId,
            @Valid @RequestBody InviteMemberRequest request) {

        // Verify the workspace IDs match (security check)
        if (!id.equals(currentWorkspaceId)) {
            throw new AccessDeniedException("Workspace ID mismatch");
        }

        // TODO: Implementation for inviting members

        return ResponseEntity.ok("Member invited");
    }
}
