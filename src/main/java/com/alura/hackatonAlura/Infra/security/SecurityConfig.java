package com.alura.hackatonAlura.Infra.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // DEV (H2 Console) - ON - prueba
        http.csrf(csrf -> csrf.disable());
        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));

        http.authorizeHttpRequests(auth -> auth
                //Solo para desarrollo con H2 Console
                .requestMatchers("/h2-console/**").permitAll()
                // Mientras se arma la seguridad real, dejamos libre el resto
                .anyRequest().permitAll()
        );


        // MySQL - ON
        // http.authorizeHttpRequests(auth -> auth
        //         // Ejemplos de endpoints públicos (ajusten a su proyecto)
        //         .requestMatchers("/login", "/usuarios", "/auth/**").permitAll()
        //         .anyRequest().authenticated()
        // );
        //
        // config de JWT o formLogin :
        // http.formLogin(Customizer.withDefaults());

        return http.build();
    }
}
