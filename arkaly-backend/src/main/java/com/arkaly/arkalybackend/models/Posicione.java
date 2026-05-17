package com.arkaly.arkalybackend.models;

import com.arkaly.arkalybackend.models.enums.TipoActivo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "posiciones", schema = "arkaly_bd", indexes = {
        @Index(name = "id_usuario", columnList = "id_usuario")
})
public class Posicione {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario idUsuario;

    @Column(name = "simbolo", nullable = false, length = 20)
    private String simbolo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_activo", nullable = false)
    private TipoActivo tipoActivo;

    @Column(name = "cantidad_total", nullable = false, precision = 18, scale = 8)
    private BigDecimal cantidadTotal;

    @Column(name = "precio_medio_compra", nullable = false, precision = 18, scale = 2)
    private BigDecimal precioMedioCompra;

    @Column(name = "total_invertido", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalInvertido;

    @Column(name = "fecha_actualizacion")
    private Instant fechaActualizacion;

}