package com.alura.hackatonAlura.service;

import com.alura.hackatonAlura.domain.usuario.DatosRegistroUsuario;
import com.alura.hackatonAlura.domain.usuario.DatosRespuestaUsuario;
import com.alura.hackatonAlura.domain.usuario.Usuario;
import com.alura.hackatonAlura.domain.usuario.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    //inyectar las dependencias con un constructor en lugar de con @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder){
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public DatosRespuestaUsuario registrar(@Valid DatosRegistroUsuario datos){
        if (usuarioRepository.existsByEmail(datos.email())) {
            throw new RuntimeException("El email ya está registrado");
        }

        String passCifrada = passwordEncoder.encode(datos.contrasena());
        var usuario = new Usuario(datos.nombre(), datos.email(), passCifrada);
        Usuario usuarioRegistrado = usuarioRepository.save(usuario);

        return new DatosRespuestaUsuario(
                usuarioRegistrado.getId(),
                usuarioRegistrado.getNombre(),
                usuarioRegistrado.getEmail());
    }
}
