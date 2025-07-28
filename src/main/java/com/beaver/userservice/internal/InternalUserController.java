package com.beaver.userservice.internal;

import com.beaver.userservice.user.User;
import com.beaver.userservice.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/validate-credentials")
    public ResponseEntity<UserCredentialsResponse> validateCredentials(
            @RequestBody CredentialsRequest request) {

        Optional<User> userOpt = userService.findByEmail(request.email());

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            if (user.isActive() && passwordEncoder.matches(request.password(), user.getPassword())) {
                return ResponseEntity.ok(UserCredentialsResponse.valid(
                        user.getId().toString(),
                        user.getEmail(),
                        user.getName(),
                        user.isActive()
                ));
            }
        }

        return ResponseEntity.ok(UserCredentialsResponse.invalid());
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
        if (userService.findByEmail(request.email()).isPresent()) {
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

            userService.saveUser(user);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
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
