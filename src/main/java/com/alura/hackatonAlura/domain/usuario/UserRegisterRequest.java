package com.alura.hackatonAlura.domain.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegisterRequest(
        @NotBlank(message = "{fullName.required}")
        String fullName,

        @NotBlank(message = "{email.required}")
        @Email(message = "{email.invalid}")
        String email,

        @NotBlank(message = "{pass.required}")
        @Size(min = 8, message = "{pass.invalid}")
        String password
) {
}
