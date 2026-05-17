package com.arkaly.arkalybackend.models;

import com.arkaly.arkalybackend.models.enums.ReglaFinanciera;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "perfiles_financieros", schema = "arkaly_bd", indexes = {
        @Index(name = "id_usuario", columnList = "id_usuario")
})
public class PerfilesFinanciero {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario idUsuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "regla_financiera", nullable = false)
    private ReglaFinanciera reglaFinanciera;

    @Column(name = "porcentaje_ahorro", precision = 5, scale = 2)
    private BigDecimal porcentajeAhorro;

    @Column(name = "fecha_actualizacion")
    private Instant fechaActualizacion;

}