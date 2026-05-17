package com.arkaly.arkalybackend.dto;

import lombok.Data;

// DTO para los activos marcados como favoritos por el usuario.
@Data
public class ActivoFavoritoDTO {
    private Integer id;
    private String simbolo;
    private String nombre;
    private String tipoActivo;
}