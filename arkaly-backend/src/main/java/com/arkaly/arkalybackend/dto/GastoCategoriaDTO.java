package com.arkaly.arkalybackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

// DTO para el desglose de gastos agrupados por categoría (usado en gráficos)
@Data
@AllArgsConstructor
public class GastoCategoriaDTO {
    private Integer idCategoria;
    private String nombreCategoria;
    private String color;
    private String icono;
    private BigDecimal total;
    private double porcentaje;
}