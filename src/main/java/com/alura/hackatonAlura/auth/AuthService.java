package com.alura.hackatonAlura.auth;

import com.alura.hackatonAlura.user.User;
import com.alura.hackatonAlura.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.alura.hackatonAlura.security.JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       com.alura.hackatonAlura.security.JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String emailLower = request.email().toLowerCase(Locale.ROOT).trim();
        if (userRepository.existsByEmail(emailLower)) {
            throw new IllegalArgumentException("Email already in use");
        }

        String hashed = passwordEncoder.encode(request.password());

        User user = new User();
        user.setEmail(emailLower);
        user.setPasswordHash(hashed);
        user.setFullName(request.fullName());
        user.setRoles("USER");

        User saved = userRepository.save(user);
        return new UserResponse(saved.getId(), saved.getEmail(), saved.getFullName(), saved.getRoles());
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String emailLower = request.email().toLowerCase(java.util.Locale.ROOT).trim();
        User user = userRepository.findByEmail(emailLower)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getEmail(), java.util.Map.of(
                "roles", user.getRoles()
        ));
        return new LoginResponse(token, "Bearer");
    }
}
