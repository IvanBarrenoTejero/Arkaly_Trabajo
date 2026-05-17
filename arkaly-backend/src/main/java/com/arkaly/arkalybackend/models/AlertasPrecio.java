package com.arkaly.arkalybackend.models;

import com.arkaly.arkalybackend.models.enums.Condicion;
import com.arkaly.arkalybackend.models.enums.TipoActivo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "alertas_precio", schema = "arkaly_bd", indexes = {
        @Index(name = "id_usuario", columnList = "id_usuario")
})
public class AlertasPrecio {
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

    @Enumerated(EnumType.STRING)
    @Column(name = "condicion", nullable = false)
    private Condicion condicion;

    @Column(name = "precio_objetivo", nullable = false, precision = 18, scale = 2)
    private BigDecimal precioObjetivo;

    @Column(name = "activa")
    private Boolean activa;

    @Column(name = "fecha_creacion")
    private Instant fechaCreacion;

    @Column(name = "fecha_disparada")
    private Instant fechaDisparada;

}