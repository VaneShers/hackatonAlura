package com.alura.hackatonAlura.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private static String normalizeEmail(String email) {
        return email.toLowerCase(Locale.ROOT).trim();
    }

    @Transactional
    public User createUser(String email, String password, String fullName, String role) {
        String emailLower = normalizeEmail(email);
        if (userRepository.existsByEmail(emailLower)) {
            throw new IllegalArgumentException("Email already in use");
        }
        if (!"ADMIN".equals(role) && !"USER".equals(role)) {
            throw new IllegalArgumentException("Invalid role");
        }

        User u = new User();
        u.setEmail(emailLower);
        u.setPasswordHash(passwordEncoder.encode(password));
        u.setFullName(fullName);
        u.setRoles(role);
        return userRepository.save(u);
    }

    @Transactional(readOnly = true)
    public List<User> listAll() {
        return userRepository.findAll();
    }

    @Transactional
    public User updateRole(Long id, String role) {
        if (!"ADMIN".equals(role) && !"USER".equals(role)) {
            throw new IllegalArgumentException("Invalid role");
        }
        User u = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        u.setRoles(role);
        return userRepository.save(u);
    }

    @Transactional
    public void deleteUser(Long id, String currentEmail) {
        User u = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (u.getEmail().equalsIgnoreCase(currentEmail)) {
            throw new IllegalArgumentException("Admins cannot delete their own account");
        }
        userRepository.delete(u);
    }

    @Transactional
    public User updateOwnEmail(String currentEmail, String newEmail) {
        String emailLower = normalizeEmail(currentEmail);
        User u = userRepository.findByEmail(emailLower).orElseThrow(() -> new IllegalArgumentException("User not found"));

        String newLower = normalizeEmail(newEmail);
        if (!newLower.equals(u.getEmail()) && userRepository.existsByEmail(newLower)) {
            throw new IllegalArgumentException("Email already in use");
        }
        u.setEmail(newLower);
        return userRepository.save(u);
    }

    @Transactional
    public User updateOwnPassword(String currentEmail, String newPassword) {
        String emailLower = normalizeEmail(currentEmail);
        User u = userRepository.findByEmail(emailLower).orElseThrow(() -> new IllegalArgumentException("User not found"));
        u.setPasswordHash(passwordEncoder.encode(newPassword));
        return userRepository.save(u);
    }
}
