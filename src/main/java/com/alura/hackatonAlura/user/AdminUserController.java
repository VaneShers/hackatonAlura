package com.alura.hackatonAlura.user;

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

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@Tag(name = "Admin", description = "Administración de usuarios (requiere rol ADMIN)")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    public static class CreateUserRequest {
        @Email @NotBlank public String email;
        @NotBlank public String password;
        @NotBlank public String fullName;
        @NotBlank public String role; // "ADMIN" or "USER"
    }

    public static class UpdateRoleRequest { @NotBlank public String role; }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear usuario", description = "Crea un usuario con rol especificado.")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest req) {
        User u = userService.createUser(req.email, req.password, req.fullName, req.role);
        URI location = URI.create("/api/users/" + u.getId());
        return ResponseEntity.created(location).body(new UserResponse(u.getId(), u.getEmail(), u.getFullName(), u.getRoles()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar usuarios", description = "Lista todos los usuarios registrados.")
    public List<UserResponse> listAll() {
        return userService.listAll().stream()
                .map(u -> new UserResponse(u.getId(), u.getEmail(), u.getFullName(), u.getRoles()))
                .toList();
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar rol", description = "Actualiza el rol del usuario (ADMIN/USER).")
    public UserResponse updateRole(@PathVariable Long id, @Valid @RequestBody UpdateRoleRequest req) {
        User u = userService.updateRole(id, req.role);
        return new UserResponse(u.getId(), u.getEmail(), u.getFullName(), u.getRoles());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar usuario", description = "Elimina un usuario por id. Los admins no pueden eliminar su propia cuenta.")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        userService.deleteUser(id, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
