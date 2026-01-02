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

    private final User.UserService userService;

    public UserProfileController(User.UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Obtener perfil", description = "Devuelve el perfil del usuario autenticado")
    public ResponseEntity<UserResponse> me(Authentication auth) {
        UserResponse res = userService.getProfile(auth.getName());
        return ResponseEntity.ok(res);
    }

    /// Añadir función de actualizar la Password
    @PutMapping
    @Operation(summary = "Actualizar perfil", description = "Actualiza email y/o nombre completo del usuario autenticado")
    public ResponseEntity<UserResponse> update(@Valid @RequestBody UpdateUserRequest req, Authentication auth) {
        UserResponse res = userService.updateProfile(auth.getName(), req);
        return ResponseEntity.ok(res);
    }

    @DeleteMapping
    @Operation(summary = "Eliminar cuenta", description = "Elimina la cuenta del usuario autenticado")
    public ResponseEntity<Void> delete(Authentication auth) {
        userService.deleteProfile(auth.getName());
        return ResponseEntity.noContent().build();
    }

    /// ROL DE ADMIN

    /// No cumple con la separación de funcionalidades
    //@GetMapping("/{id}")
    //@Operation(summary = "Obtener usuario por id", description = "Devuelve un usuario por su identificador")
    //public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
       // return userRepository.findById(id)
               // .map(u -> ResponseEntity.ok(new UserResponse(u.getId(), u.getEmail(), u.getFullName(), u.getRoles())))
               // .orElseGet(() -> ResponseEntity.notFound().build());
    //}

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
    public ResponseEntity<Void> updateRole() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    //public UserResponse updateRole(@PathVariable Long id, @Valid @RequestBody AdminUserController.UpdateRoleRequest req) {
     //   User u = userService.updateRole(id, req.role);
       // return new UserResponse(u.getId(), u.getEmail(), u.getFullName(), u.getRoles());
   // }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar usuario", description = "Elimina un usuario por id. Los admins no pueden eliminar su propia cuenta.")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        userService.deleteUser(id, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
