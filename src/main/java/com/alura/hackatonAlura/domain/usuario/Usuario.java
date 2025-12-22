package com.alura.hackatonAlura.domain.usuario;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "Usuario")/*Marca la clase como entidad*/
@Table(name = "usuarios")/*Cambia nombre a la tabla*/
@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Usuario {
    @Id/*Indica que esle id*/
    @GeneratedValue(strategy = GenerationType.IDENTITY)/*Id incremental*/
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(unique = true, nullable = false)//email único
    private String email;

    @Column(nullable = false)
    private String contrasena;
    //private String role;

    //Constructor para el registro ya que no puse setters
    public Usuario(String nombre, String email, String contrasena){
        this.nombre = nombre;
        this.email = email;
        this.contrasena = contrasena;
    }
}
