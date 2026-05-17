package com.arkaly.arkalybackend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "likes_mensajes", schema = "arkaly_bd", indexes = {
        @Index(name = "id_mensaje", columnList = "id_mensaje")
})
public class LikesMensaje {
    @EmbeddedId
    private LikesMensajeId id;

    @MapsId("idUsuario")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario idUsuario;

    @MapsId("idMensaje")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_mensaje", nullable = false)
    private Mensaje idMensaje;

    @Column(name = "fecha")
    private Instant fecha;

}