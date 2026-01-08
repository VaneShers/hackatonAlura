package com.alura.hackatonAlura.auth;

import com.alura.hackatonAlura.user.User;
import com.alura.hackatonAlura.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    AuthService authService;

    @Test
    void registerThrowsOnDuplicateEmail() {
        RegisterRequest req = new RegisterRequest("Test@Example.com", "secret", "Name");
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> authService.register(req));
        assertEquals("Email already in use", ex.getMessage());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerHashesPasswordAndSavesUser() {
        RegisterRequest req = new RegisterRequest("User@Mail.com", "password123", "Full Name");
        when(userRepository.existsByEmail("user@mail.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("HASHED-PW");

        User saved = new User();
        saved.setId(1L);
        saved.setEmail("user@mail.com");
        saved.setPasswordHash("HASHED-PW");
        saved.setFullName("Full Name");
        saved.setRoles("USER");

        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserResponse res = authService.register(req);

        assertNotNull(res);
        assertEquals(1L, res.id());
        assertEquals("user@mail.com", res.email());
        assertEquals("Full Name", res.fullName());
        assertEquals("USER", res.roles());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User toSave = captor.getValue();
        assertEquals("user@mail.com", toSave.getEmail());
        assertEquals("HASHED-PW", toSave.getPasswordHash());
        assertEquals("USER", toSave.getRoles());
    }
}
