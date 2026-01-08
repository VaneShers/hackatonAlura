package com.alura.hackatonAlura.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Email @NotBlank String email,
        @NotBlank @Size(min = 8, max = 20) String password,
        @NotBlank @Size(min = 5, max = 50) String fullName
) {}
