package com.arkaly.arkalybackend.models;

import com.arkaly.arkalybackend.models.enums.TipoActivo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "activos_favoritos", schema = "arkaly_bd", indexes = {
        @Index(name = "id_usuario", columnList = "id_usuario")
})
public class ActivosFavorito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario idUsuario;

    @Column(name = "simbolo", nullable = false, length = 20)
    private String simbolo;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_activo", nullable = false)
    private TipoActivo tipoActivo;

    @Column(name = "fecha_agregado")
    private Instant fechaAgregado;

}