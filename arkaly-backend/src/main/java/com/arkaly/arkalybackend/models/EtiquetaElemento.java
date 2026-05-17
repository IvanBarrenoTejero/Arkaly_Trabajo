package com.arkaly.arkalybackend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "etiqueta_elemento", schema = "arkaly_bd", indexes = {
        @Index(name = "id_etiqueta", columnList = "id_etiqueta"),
        @Index(name = "id_carpeta", columnList = "id_carpeta"),
        @Index(name = "id_documento", columnList = "id_documento")
})
public class EtiquetaElemento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_etiqueta", nullable = false)
    private Etiqueta idEtiqueta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_carpeta")
    private Carpeta idCarpeta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_documento")
    private Documento idDocumento;

}