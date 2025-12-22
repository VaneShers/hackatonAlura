package com.alura.hackatonAlura.domain.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DatosRegistroUsuario(
        @NotBlank(message = "{nombre.obligatorio}")
        String nombre,

        @NotBlank(message = "{email.obligatorio}")
        @Email(message = "{email.invalido}")
        String email,

        @NotBlank(message = "{pass.obligatorio}")
        @Size(min = 8, message = "{pass.invalido}")
        String contrasena
) {
}
