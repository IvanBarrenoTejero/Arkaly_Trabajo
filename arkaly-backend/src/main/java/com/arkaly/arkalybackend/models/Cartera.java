package com.arkaly.arkalybackend.models;

import com.arkaly.arkalybackend.models.enums.TipoActivo;
import com.arkaly.arkalybackend.models.enums.TipoOperacion;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "cartera", schema = "arkaly_bd", indexes = {
        @Index(name = "id_usuario", columnList = "id_usuario")
})
public class Cartera {
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
    @Column(name = "tipo_operacion", nullable = false)
    private TipoOperacion tipoOperacion;

    @Column(name = "cantidad", nullable = false, precision = 18, scale = 8)
    private BigDecimal cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 18, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "precio_total", nullable = false, precision = 18, scale = 2)
    private BigDecimal precioTotal;

    @Column(name = "fecha_operacion", nullable = false)
    private LocalDate fechaOperacion;

    @Column(name = "notas")
    private String notas;

}