package com.alura.hackatonAlura.auth;

public record UserResponse(
        Long id,
        String email,
        String fullName,
        String roles
) {}
