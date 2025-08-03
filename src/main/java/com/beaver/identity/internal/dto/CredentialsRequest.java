package com.beaver.identity.internal.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

@Builder
public record CredentialsRequest(
        @NotEmpty
        @Email
        String email,
        @NotEmpty
        String password
) {}
