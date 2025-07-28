package com.beaver.userservice.internal;

import com.beaver.userservice.common.exception.InvalidUserDataException;
import com.beaver.userservice.common.exception.UserAlreadyExistsException;
import com.beaver.userservice.internal.dto.CreateUserRequest;
import com.beaver.userservice.internal.dto.CredentialsRequest;
import com.beaver.userservice.user.entity.User;
import com.beaver.userservice.user.UserService;
import com.beaver.userservice.user.dto.UserDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/validate-credentials")
    public ResponseEntity<UserDto> validateCredentials(@Valid @RequestBody CredentialsRequest request) {

        User user = userService.findByEmail(request.email())
                .orElseThrow(() -> new InvalidUserDataException("Invalid credentials"));

        if (user.isActive() && passwordEncoder.matches(request.password(), user.getPassword())) {
            return ResponseEntity.ok(UserDto.fromEntity(user));
        }

        throw new InvalidUserDataException("Invalid credentials");
    }

    @PostMapping("/users")
    public ResponseEntity<Void> createUser(@Valid @RequestBody CreateUserRequest request) {
        if (userService.findByEmail(request.email()).isPresent()) {
            throw new UserAlreadyExistsException(request.email());
        }

        try {
            User user = User.builder()
                    .email(request.email())
                    .password(passwordEncoder.encode(request.password()))
                    .name(request.name())
                    .isActive(true)
                    .build();

            userService.saveUser(user);
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
}
