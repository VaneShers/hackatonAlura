package com.alura.hackatonAlura.infra.security;

import com.alura.hackatonAlura.domain.user.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class TokenService {

    @Value("${api.security.secret}")
    private String secret;

    public String generarToken(User usuario) {
        return JWT.create()
                .withIssuer("login-api")
                .withSubject(usuario.getUsername())
                .withClaim("rol", usuario.getRol().name())
                .withExpiresAt(Instant.now().plus(2, ChronoUnit.HOURS))
                .sign(Algorithm.HMAC256(secret));
    }

    public String getSubject(String token) {
        return JWT.require(Algorithm.HMAC256(secret))
                .withIssuer("login-api")
                .build()
                .verify(token)
                .getSubject();
    }
}