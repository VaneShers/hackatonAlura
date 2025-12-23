package com.alura.hackatonAlura.domain.usuario;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "User")/*Marca la clase como entidad*/
@Table(name = "users")/*Cambia fullName a la tabla*/
@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class User {
    @Id/*Indica que esle id*/
    @GeneratedValue(strategy = GenerationType.IDENTITY)/*Id incremental*/
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(unique = true, nullable = false)//email único
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    //Constructor para el registro ya que no puse setters
    public User(String fullName, String email, String password){
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.status = Status.ACTIVE;
        this.role = Role.USER;
    }
}
