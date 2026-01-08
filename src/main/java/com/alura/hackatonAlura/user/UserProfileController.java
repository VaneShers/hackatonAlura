package com.alura.hackatonAlura.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.alura.hackatonAlura.auth.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User", description = "Operaciones del usuario autenticado")
public class UserProfileController {

    private final UserService userService;

    public UserProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Obtener perfil", description = "Devuelve el perfil del usuario autenticado")
    public ResponseEntity<UserResponse> me(Authentication auth) {
        UserResponse res = userService.getProfile(auth.getName());
        return ResponseEntity.ok(res);
    }

    @PutMapping
    @Operation(summary = "Actualizar perfil", description = "Actualiza email y/o nombre completo del usuario autenticado")
    public ResponseEntity<UserResponse> update(@Valid @RequestBody UpdateUserRequest req, Authentication auth) {
        UserResponse res = userService.updateProfile(auth.getName(), req);
        return ResponseEntity.ok(res);
    }

    @PutMapping("/password")
    @Operation(
            summary = "Actualizar contraseña",
            description = "Actualiza la contraseña del usuario autenticado"
    )
    public ResponseEntity<Void> updatePassword(@Valid @RequestBody UpdatePasswordRequest req, Authentication auth) {
        userService.updatePassword(auth.getName(), req);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @Operation(summary = "Eliminar cuenta", description = "Elimina la cuenta del usuario autenticado")
    public ResponseEntity<Void> delete(Authentication auth) {
        userService.deleteProfile(auth.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener usuario por id", description = "Devuelve un usuario por su identificador")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/all")
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
    public ResponseEntity<UserResponse> updateRole(@PathVariable Long id,@Valid @RequestBody UpdateRoleRequest req) {
        User u = userService.updateRole(id, req.role());
        return ResponseEntity.ok(
                new UserResponse(u.getId(), u.getEmail(), u.getFullName(), u.getRoles())
        );
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar usuario", description = "Elimina un usuario por id. Los admins no pueden eliminar su propia cuenta.")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        userService.deleteUser(id, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
