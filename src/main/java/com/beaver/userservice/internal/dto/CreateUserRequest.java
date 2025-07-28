package com.beaver.userservice.internal.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

@Builder
public record CreateUserRequest(
        @NotEmpty
        @Email
        String email,
        @NotEmpty
        String password,
        @NotEmpty
        String name
) {}
