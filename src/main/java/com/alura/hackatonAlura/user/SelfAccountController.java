package com.alura.hackatonAlura.user;

import com.alura.hackatonAlura.auth.UserResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me")
public class SelfAccountController {

    private final UserService userService;

    public SelfAccountController(UserService userService) {
        this.userService = userService;
    }

    public static class UpdateEmailRequest { @Email @NotBlank public String email; }
    public static class UpdatePasswordRequest { @NotBlank public String password; }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public UserResponse me(Authentication auth) {
        User u = userService.listAll().stream()
                .filter(x -> x.getEmail().equalsIgnoreCase(auth.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return new UserResponse(u.getId(), u.getEmail(), u.getFullName(), u.getRoles());
    }

    @PutMapping("/email")
    @PreAuthorize("isAuthenticated()")
    public UserResponse updateEmail(Authentication auth, @Valid @RequestBody UpdateEmailRequest req) {
        User u = userService.updateOwnEmail(auth.getName(), req.email);
        return new UserResponse(u.getId(), u.getEmail(), u.getFullName(), u.getRoles());
    }

    @PutMapping("/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> updatePassword(Authentication auth, @Valid @RequestBody UpdatePasswordRequest req) {
        userService.updateOwnPassword(auth.getName(), req.password);
        return ResponseEntity.noContent().build();
    }
}
