package com.alura.hackatonAlura.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.alura.hackatonAlura.auth.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me")
@Tag(name = "Profile", description = "Operaciones del perfil del usuario autenticado")
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

    @DeleteMapping
    @Operation(summary = "Eliminar cuenta", description = "Elimina la cuenta del usuario autenticado")
    public ResponseEntity<Void> delete(Authentication auth) {
        userService.deleteProfile(auth.getName());
        return ResponseEntity.noContent().build();
    }
}
