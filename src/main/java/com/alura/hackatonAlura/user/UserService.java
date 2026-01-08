package com.alura.hackatonAlura.user;

import com.alura.hackatonAlura.auth.UserResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
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

        if (req.fullName() != null && !req.fullName().isBlank()) {
            user.setFullName(req.fullName());
        }

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
    public User createUser(String email, String password, String fullName, Role role) {
        String emailLower = email.toLowerCase(Locale.ROOT).trim();
        if (userRepository.existsByEmail(emailLower)) {
            throw new IllegalArgumentException("Email already in use");
        }
        String hashed = passwordEncoder.encode(password);
        User u = new User();
        u.setEmail(emailLower);
        u.setPasswordHash(hashed);
        u.setFullName(fullName);
        u.setRoles(role != null ? role : Role.USER);

        return userRepository.save(u);
    }

    @Transactional
    public User updateRole(Long id, Role role) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        u.setRoles(role);
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

    @Transactional
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRoles()
        );
    }

    public void updatePassword(String userId, @Valid UpdatePasswordRequest req) {
        Long id = Long.parseLong(userId);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
    }
}

