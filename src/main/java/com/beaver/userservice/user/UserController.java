package com.beaver.userservice.user;

import com.beaver.userservice.membership.MembershipService;
import com.beaver.userservice.membership.entity.WorkspaceMembership;
import com.beaver.userservice.user.dto.UpdateSelf;
import com.beaver.userservice.user.dto.UserDto;
import com.beaver.userservice.user.entity.User;
import com.beaver.userservice.workspace.dto.WorkspaceListDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
public class UserController {

    private final UserService userService;
    private final MembershipService membershipService;

    @GetMapping(value = "/self", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDto> getSelf(@RequestHeader("X-User-Id") UUID id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(UserDto.fromEntity(user));
    }

    @PatchMapping(value = "/self", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDto> updateSelf(
            @RequestHeader("X-User-Id") UUID id,
            @Valid @RequestBody UpdateSelf updateSelf)
    {
        User user = userService.updateSelf(id, updateSelf);
        return ResponseEntity.ok(UserDto.fromEntity(user));
    }

    @DeleteMapping(value = "/self")
    public ResponseEntity<Void> deleteSelf(@RequestHeader("X-User-Id") UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/self/workspaces", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<WorkspaceListDto>> getUserWorkspaces(@RequestHeader("X-User-Id") UUID userId) {
        List<WorkspaceMembership> memberships = membershipService.findActiveByUserId(userId);
        return ResponseEntity.ok(WorkspaceListDto.fromMemberships(memberships);
    }
}
