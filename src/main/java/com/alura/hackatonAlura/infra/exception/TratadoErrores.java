package com.alura.hackatonAlura.infra.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TratadoErrores {

    // 404 - Recurso no encontrado
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> tratarError404() {
        return ResponseEntity.notFound().build();
    }

    // 401 - Error de autenticación (login incorrecto)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> tratarErrorDeAutenticacion(AuthenticationException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Usuario o contraseña incorrectos");
    }

    // 400 - Errores de validación DTO
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> tratarError400(MethodArgumentNotValidException e) {
        var errores = e.getFieldErrors()
                .stream()
                .map(DatosErrorValidacion::new)
                .toList();
        return ResponseEntity.badRequest().body(errores);
    }

    // 400 - Validaciones de integridad
    @ExceptionHandler(ValidacionDeIntegridad.class)
    public ResponseEntity<?> errorHanddlerValidacionesDeIntegridad(ValidacionDeIntegridad e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    // 400 - Validaciones de negocio
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<?> errorHanddlerValidacionesDeNegocio(ValidationException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    // 401 - Token inválido / expirado
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> tratarTokenInvalido(RuntimeException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(e.getMessage());
    }

    // 403 - Acceso denegado por rol
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> tratarAccesoDenegado() {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body("No tienes permisos para acceder a este recurso");
    }

    private record DatosErrorValidacion(String campo, String error) {
        public DatosErrorValidacion(FieldError error) {
            this(error.getField(), error.getDefaultMessage());
        }
    }
}