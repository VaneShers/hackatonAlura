package com.alura.hackatonAlura.domain.usuario;

public record UserResponse(
        Long id,
        String nombre,
        String email,
        Status status,
        Role role) {

}
