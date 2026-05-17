package com.arkaly.arkalybackend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "mensajes", schema = "arkaly_bd", indexes = {
        @Index(name = "id_usuario", columnList = "id_usuario"),
        @Index(name = "id_hilo", columnList = "id_hilo"),
        @Index(name = "eliminado_por", columnList = "eliminado_por")
})
public class Mensaje {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario idUsuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_hilo", nullable = false)
    private Hilo idHilo;

    @Column(name = "contenido", nullable = false, columnDefinition = "TEXT")
    private String contenido;

    @Column(name = "eliminado")
    private Boolean eliminado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eliminado_por")
    private Usuario eliminadoPor;

    @Column(name = "fecha_creacion")
    private Instant fechaCreacion;

    @Column(name = "fecha_edicion")
    private Instant fechaEdicion;

}