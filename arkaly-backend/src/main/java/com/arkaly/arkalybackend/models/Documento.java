package com.arkaly.arkalybackend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "documentos", schema = "arkaly_bd", indexes = {
        @Index(name = "id_usuario", columnList = "id_usuario"),
        @Index(name = "id_carpeta", columnList = "id_carpeta")
})
public class Documento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario idUsuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_carpeta")
    private Carpeta idCarpeta;

    @Column(name = "nombre_original", nullable = false)
    private String nombreOriginal;

    @Column(name = "nombre_archivo", nullable = false)
    private String nombreArchivo;

    @Column(name = "ruta_relativa", nullable = false, length = 500)
    private String rutaRelativa;

    @Column(name = "tipo_mime", length = 100)
    private String tipoMime;

    @Column(name = "tamanio_bytes")
    private Long tamanioBytes;

    @Column(name = "categoria", length = 100)
    private String categoria;

    @Column(name = "fecha_subida")
    private Instant fechaSubida;

    @PrePersist
    protected void onCreate() {
        fechaSubida = java.time.Instant.now();
    }
}