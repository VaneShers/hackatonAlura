package com.alura.hackatonAlura.auth;

import com.alura.hackatonAlura.user.Role;

public record UserResponse(
        Long id,
        String email,
        String fullName,
        Role roles
) {}
