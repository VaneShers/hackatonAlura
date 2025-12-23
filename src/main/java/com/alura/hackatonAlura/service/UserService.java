package com.alura.hackatonAlura.service;

import com.alura.hackatonAlura.domain.usuario.UserRegisterRequest;
import com.alura.hackatonAlura.domain.usuario.UserResponse;
import com.alura.hackatonAlura.domain.usuario.User;
import com.alura.hackatonAlura.domain.usuario.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    //inyectar las dependencias con un constructor en lugar de con @Autowired
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse register(@Valid UserRegisterRequest datos){
        if (userRepository.existsByEmail(datos.email())) {
            throw new RuntimeException("El email ya está registrado");
        }

        String hash = passwordEncoder.encode(datos.password());
        var user = new User(datos.fullName(), datos.email(), hash);
        User savedUser = userRepository.save(user);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getFullName(),
                savedUser.getEmail(),
                savedUser.getStatus(),
                savedUser.getRole());
    }
}
