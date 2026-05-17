package com.arkaly.arkalybackend.dto;

import lombok.Data;

// DTO para los activos disponibles en el mercado (acciones, ETFs, crypto...)
@Data
public class ActivoMercadoDTO {
    private Integer id;
    private String simbolo;
    private String nombre;
    private String tipoActivo;
}