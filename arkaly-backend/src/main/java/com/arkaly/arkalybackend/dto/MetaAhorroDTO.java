package com.arkaly.arkalybackend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

// DTO para las metas de ahorro del usuario.
@Data
public class MetaAhorroDTO {
    private Integer id;
    private String nombre;
    private BigDecimal cantidadObjetivo;
    private BigDecimal cantidadActual;
    private LocalDate fechaLimite;
    private Boolean completada;
    private LocalDate fechaCreacion;
}