package com.arkaly.arkalybackend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "hilos", schema = "arkaly_bd", indexes = {
        @Index(name = "id_usuario", columnList = "id_usuario"),
        @Index(name = "id_categoria", columnList = "id_categoria")
})
public class Hilo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario idUsuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_categoria", nullable = false)
    private CategoriasForo idCategoria;

    @Column(name = "titulo", nullable = false)
    private String titulo;

    @Column(name = "vistas")
    private Integer vistas;

    @Column(name = "fijado")
    private Boolean fijado;

    @Column(name = "cerrado")
    private Boolean cerrado;

    @Column(name = "fecha_creacion")
    private Instant fechaCreacion;

    @Column(name = "fecha_ultimo_mensaje")
    private Instant fechaUltimoMensaje;

}