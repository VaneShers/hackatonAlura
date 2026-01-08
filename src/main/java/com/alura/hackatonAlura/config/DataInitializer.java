package com.alura.hackatonAlura.config;

import com.alura.hackatonAlura.user.User;
import com.alura.hackatonAlura.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Locale;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder encoder,
                                @Value("${admin.initial.email:admin@local}") String adminEmail,
                                @Value("${admin.initial.password:Admin123!}") String adminPassword,
                                @Value("${admin.initial.full-name:Initial Admin}") String adminFullName) {
        return args -> {
            String emailLower = adminEmail.toLowerCase(Locale.ROOT).trim();
            boolean hasAdmin = userRepository.findAll().stream().anyMatch(u -> "ADMIN".equalsIgnoreCase(u.getRoles()));
            if (!hasAdmin) {
                if (userRepository.existsByEmail(emailLower)) {
                    // Upgrade existing user to ADMIN if email matches
                    userRepository.findByEmail(emailLower).ifPresent(u -> {
                        u.setRoles("ADMIN");
                        userRepository.save(u);
                    });
                } else {
                    User admin = new User();
                    admin.setEmail(emailLower);
                    admin.setPasswordHash(encoder.encode(adminPassword));
                    admin.setFullName(adminFullName);
                    admin.setRoles("ADMIN");
                    userRepository.save(admin);
                }
            }
        };
    }
}
