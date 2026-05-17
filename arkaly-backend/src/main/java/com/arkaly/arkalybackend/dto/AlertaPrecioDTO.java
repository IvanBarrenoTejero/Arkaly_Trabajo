package com.arkaly.arkalybackend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

// DTO para las alertas de precio configuradas por el usuario.
@Data
public class AlertaPrecioDTO {
    private Integer id;
    private String nombre;
    private String simbolo;
    private String tipoActivo;
    private String condicion;
    private BigDecimal precioObjetivo;
    private Boolean activa;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaDisparada;  // null si aún no se ha disparado
    private BigDecimal precioActual;
    private Boolean disparada;
}