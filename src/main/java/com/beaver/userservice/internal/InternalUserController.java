package com.beaver.userservice.internal;

import com.beaver.userservice.user.User;
import com.beaver.userservice.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
@Slf4j
public class InternalUserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/validate-credentials")
    public ResponseEntity<UserCredentialsResponse> validateCredentials(
            @RequestBody CredentialsRequest request) {

        log.info("Validating credentials for email: {}", request.email());

        Optional<User> userOpt = userService.findByEmail(request.email());

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            log.info("User found - email: {}, isActive: {}, passwordHash present: {}",
                    user.getEmail(), user.isActive(), user.getPassword() != null);

            boolean passwordMatches = passwordEncoder.matches(request.password(), user.getPassword());
            log.info("Password match result: {}", passwordMatches);

            if (user.isActive() && passwordMatches) {
                log.info("Validation successful for user: {}", user.getEmail());
                return ResponseEntity.ok(UserCredentialsResponse.valid(
                        user.getId().toString(),
                        user.getEmail(),
                        user.getName(),
                        user.isActive()
                ));
            } else {
                log.warn("Validation failed - isActive: {}, passwordMatches: {}", user.isActive(), passwordMatches);
            }
        } else {
            log.warn("User not found for email: {}", request.email());
        }

        return ResponseEntity.ok(UserCredentialsResponse.invalid());
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
        log.info("Creating user with email: {}", request.email());

        if (userService.findByEmail(request.email()).isPresent()) {
            log.warn("User already exists with email: {}", request.email());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "User already exists with this email"));
        }

        try {
            User user = User.builder()
                    .email(request.email())
                    .password(passwordEncoder.encode(request.password()))
                    .name(request.name())
                    .isActive(true)
                    .build();

            User savedUser = userService.saveUser(user);
            log.info("Successfully created user with email: {}, id: {}", savedUser.getEmail(), savedUser.getId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to create user with email: {}", request.email(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to create user: " + e.getMessage()));
        }
    }

    // DTOs
    public record CredentialsRequest(String email, String password) {}
    public record CreateUserRequest(String email, String password, String name) {}

    public record UserCredentialsResponse(
            boolean isValid, String userId, String email, String name, boolean isActive) {

        public static UserCredentialsResponse invalid() {
            return new UserCredentialsResponse(false, null, null, null, false);
        }

        public static UserCredentialsResponse valid(String userId, String email, String name, boolean isActive) {
            return new UserCredentialsResponse(true, userId, email, name, isActive);
        }
    }
}
