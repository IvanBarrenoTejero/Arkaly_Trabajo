package com.arkaly.arkalybackend.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class LikesMensajeId implements Serializable {
    private static final long serialVersionUID = -4182802825696336628L;
    @Column(name = "id_usuario", nullable = false)
    private Integer idUsuario;

    @Column(name = "id_mensaje", nullable = false)
    private Integer idMensaje;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        LikesMensajeId entity = (LikesMensajeId) o;
        return Objects.equals(this.idMensaje, entity.idMensaje) &&
                Objects.equals(this.idUsuario, entity.idUsuario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idMensaje, idUsuario);
    }

}