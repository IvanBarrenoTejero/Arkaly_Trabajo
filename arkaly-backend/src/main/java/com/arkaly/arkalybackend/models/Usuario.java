package com.arkaly.arkalybackend.models;

import com.arkaly.arkalybackend.models.enums.Rol;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "usuarios", schema = "arkaly_bd", uniqueConstraints = {
        @UniqueConstraint(name = "username", columnNames = {"username"}),
        @UniqueConstraint(name = "email", columnNames = {"email"})
})
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol")
    private Rol rol;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "activo")
    private Boolean activo;

    @Column(name = "fecha_registro")
    private Instant fechaRegistro;

    @Column(name = "ultimo_acceso")
    private Instant ultimoAcceso;

}