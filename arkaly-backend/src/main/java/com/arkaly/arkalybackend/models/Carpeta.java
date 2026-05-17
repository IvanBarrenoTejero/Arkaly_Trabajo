package com.arkaly.arkalybackend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "carpetas", schema = "arkaly_bd", indexes = {
        @Index(name = "id_usuario", columnList = "id_usuario"),
        @Index(name = "id_carpeta_padre", columnList = "id_carpeta_padre")
})
public class Carpeta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario idUsuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_carpeta_padre")
    private Carpeta idCarpetaPadre;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "fecha_creacion")
    private Instant fechaCreacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = java.time.Instant.now();
    }
}