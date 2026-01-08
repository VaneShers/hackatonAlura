package com.alura.hackatonAlura.auth;

public record LoginResponse(
        String token,
        String tokenType
) {}
