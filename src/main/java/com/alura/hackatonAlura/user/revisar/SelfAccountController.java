/* package com.alura.hackatonAlura.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

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
@Tag(name = "Profile", description = "Autogestión del usuario autenticado")
public class SelfAccountController {

    private final User.UserService userService;

    public SelfAccountController(User.UserService userService) {
        this.userService = userService;
    }

    public static class UpdateEmailRequest { @Email @NotBlank public String email; }
    public static class UpdatePasswordRequest { @NotBlank public String password; }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mi perfil", description = "Devuelve el perfil del usuario autenticado")
    public UserResponse me(Authentication auth) {
        UserResponse res = userService.getProfile(auth.getName());
        return res;
    }


    @PutMapping("/email")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Actualizar email", description = "Actualiza el email del usuario autenticado")
    public UserResponse updateEmail(Authentication auth, @Valid @RequestBody UpdateEmailRequest req) {
        User u = userService.updateOwnEmail(auth.getName(), req.email);
        return new UserResponse(u.getId(), u.getEmail(), u.getFullName(), u.getRoles());
    }

    @PutMapping("/password")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Actualizar contraseña", description = "Actualiza la contraseña del usuario autenticado")
    public ResponseEntity<Void> updatePassword(Authentication auth, @Valid @RequestBody UpdatePasswordRequest req) {
        userService.updateOwnPassword(auth.getName(), req.password);
        return ResponseEntity.noContent().build();
    }
}

 */
