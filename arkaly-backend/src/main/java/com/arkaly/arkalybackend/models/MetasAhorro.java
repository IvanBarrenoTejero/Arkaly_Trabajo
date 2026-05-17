package com.arkaly.arkalybackend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "metas_ahorro", schema = "arkaly_bd", indexes = {
        @Index(name = "id_usuario", columnList = "id_usuario")
})
public class MetasAhorro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario idUsuario;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "cantidad_objetivo", nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidadObjetivo;

    @Column(name = "cantidad_actual", precision = 10, scale = 2)
    private BigDecimal cantidadActual;

    @Column(name = "fecha_limite")
    private LocalDate fechaLimite;

    @Column(name = "completada")
    private Boolean completada;

    @Column(name = "fecha_creacion")
    private Instant fechaCreacion;

}