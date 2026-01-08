package com.alura.hackatonAlura.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePasswordRequest(
        @NotBlank
        @Size(min = 8, max = 20, message = "Password must be at least 8 characters")
        String newPassword
) {
}
