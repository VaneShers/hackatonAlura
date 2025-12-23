package com.alura.hackatonAlura.domain.usuario;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);//para buscar en la BD
    boolean existsByEmail(String email); //para evitar duplicados en el registro
}
