package com.beaver.userservice.user;

import com.beaver.userservice.user.dto.UserDto;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(value = "/self", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDto> getSelf(@RequestHeader("X-User-Id") UUID id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(UserDto.fromEntity(user));
    }

//    @PatchMapping(value = "/self", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<UserDto> updateSelf(
//            Authentication authentication,
//            @Valid @RequestBody UpdateSelf updateProfileRequest)
//    {
//        String email = authentication.getName();
//        User user = userService.updateSelf(email, updateProfileRequest);
//        return ResponseEntity.ok(UserDto.fromEntity(user));
//    }

    @DeleteMapping(value = "/self")
    public ResponseEntity<Void> deleteSelf(@RequestHeader("X-User-Id") UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}

