package com.beaver.userservice.auth.exception;

import com.beaver.auth.exceptions.InvalidRefreshTokenException;
import com.beaver.auth.exceptions.AuthenticationFailedException;
import com.beaver.auth.exceptions.JwtTokenMalformedException;
import com.beaver.auth.exceptions.JwtTokenMissingException;
import com.beaver.userservice.auth.dto.AuthResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class AuthExceptionHandler {

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<AuthResponse> handleInvalidRefreshToken(InvalidRefreshTokenException ex) {
        log.warn("Invalid refresh token: {}", ex.getMessage());
        return ResponseEntity.status(401).body(
            AuthResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .build()
        );
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<AuthResponse> handleAuthenticationFailed(AuthenticationFailedException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return ResponseEntity.status(401).body(
            AuthResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .build()
        );
    }

    @ExceptionHandler(JwtTokenMalformedException.class)
    public ResponseEntity<AuthResponse> handleMalformedToken(JwtTokenMalformedException ex) {
        log.warn("Malformed JWT token: {}", ex.getMessage());
        return ResponseEntity.status(401).body(
            AuthResponse.builder()
                .success(false)
                .message("Invalid token format")
                .build()
        );
    }

    @ExceptionHandler(JwtTokenMissingException.class)
    public ResponseEntity<AuthResponse> handleMissingToken(JwtTokenMissingException ex) {
        log.warn("Missing JWT token: {}", ex.getMessage());
        return ResponseEntity.status(401).body(
            AuthResponse.builder()
                .success(false)
                .message("Access token is required")
                .build()
        );
    }
}
