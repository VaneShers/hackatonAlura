package com.alura.hackatonAlura.user;

import com.alura.hackatonAlura.auth.UserResponse;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_email", columnNames = {"email"})
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String roles = "USER";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public User() {
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (roles == null || roles.isBlank()) {
            roles = "USER";
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Service
    public static class UserService {

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;

        public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
            this.userRepository = userRepository;
            this.passwordEncoder = passwordEncoder;
        }

        @Transactional(readOnly = true)
        public UserResponse getProfile(String userId) {
            Long id = Long.parseLong(userId);
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            return new UserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getRoles());
        }

        @Transactional
        public UserResponse updateProfile(String userId, UpdateUserRequest req) {
            Long id = Long.parseLong(userId);
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            if (req.email() != null && !req.email().isBlank()) {
                String newEmail = req.email().toLowerCase(Locale.ROOT).trim();
                if (!newEmail.equals(user.getEmail()) && userRepository.existsByEmail(newEmail)) {
                    throw new IllegalArgumentException("Email already in use");
                }
                user.setEmail(newEmail);
            }

            user.setFullName(req.fullName());

            User saved = userRepository.save(user);
            return new UserResponse(saved.getId(), saved.getEmail(), saved.getFullName(), saved.getRoles());
        }

        @Transactional
        public void deleteProfile(String userId) {
            Long id = Long.parseLong(userId);
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            userRepository.delete(user);
        }

        @Transactional(readOnly = true)
        public List<User> listAll() {
            return userRepository.findAll();
        }

        @Transactional
        public User createUser(String email, String password, String fullName, String role) {
            String emailLower = email.toLowerCase(Locale.ROOT).trim();
            if (userRepository.existsByEmail(emailLower)) {
                throw new IllegalArgumentException("Email already in use");
            }
            String hashed = passwordEncoder.encode(password);
            String roleNorm = role.toUpperCase(Locale.ROOT).trim();
            if (!roleNorm.equals("ADMIN") && !roleNorm.equals("USER")) {
                throw new IllegalArgumentException("Invalid role");
            }
            User u = new User();
            u.setEmail(emailLower);
            u.setPasswordHash(hashed);
            u.setFullName(fullName);
            u.setRoles(roleNorm);
            return userRepository.save(u);
        }

        @Transactional
        public User updateRole(Long id, String role) {
            User u = userRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            String roleNorm = role.toUpperCase(Locale.ROOT).trim();
            if (!roleNorm.equals("ADMIN") && !roleNorm.equals("USER")) {
                throw new IllegalArgumentException("Invalid role");
            }
            u.setRoles(roleNorm);
            return userRepository.save(u);
        }

        @Transactional
        public void deleteUser(Long id, String currentUserId) {
            Long currentId = Long.parseLong(currentUserId);
            if (id.equals(currentId)) {
                throw new IllegalArgumentException("Admins cannot delete their own account");
            }
            User u = userRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            userRepository.delete(u);
        }

        @Transactional
        public User updateOwnEmail(String userId, String newEmail) {
            Long id = Long.parseLong(userId);
            User u = userRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            String emailLower = newEmail.toLowerCase(Locale.ROOT).trim();
            if (!emailLower.equals(u.getEmail()) && userRepository.existsByEmail(emailLower)) {
                throw new IllegalArgumentException("Email already in use");
            }
            u.setEmail(emailLower);
            return userRepository.save(u);
        }

        @Transactional
        public void updateOwnPassword(String userId, String newPassword) {
            Long id = Long.parseLong(userId);
            User u = userRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            String hashed = passwordEncoder.encode(newPassword);
            u.setPasswordHash(hashed);
            userRepository.save(u);
        }
    }
}
