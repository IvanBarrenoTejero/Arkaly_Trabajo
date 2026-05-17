package com.arkaly.arkalybackend.models;

import com.arkaly.arkalybackend.models.enums.TipoActivo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "activos_mercado", schema = "arkaly_bd", uniqueConstraints = {
        @UniqueConstraint(name = "uk_simbolo", columnNames = {"simbolo"})
})
public class ActivosMercado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "simbolo", nullable = false, length = 20)
    private String simbolo;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_activo", nullable = false)
    private TipoActivo tipoActivo;

    @Column(name = "es_default")
    private Boolean esDefault;
}