package com.alura.hackatonAlura.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequest(
        @Email String email,
        @NotBlank String fullName
) {}
