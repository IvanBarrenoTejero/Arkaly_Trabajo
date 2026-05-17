package com.arkaly.arkalybackend.dto;

import com.arkaly.arkalybackend.models.enums.TipoTransaccion;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

// DTO para las transacciones económicas del usuario (ingresos y gastos)
@Data
public class TransaccionDTO {
    private Integer id;
    private Integer idCategoria;
    private String nombreCategoria;
    private String colorCategoria;
    private TipoTransaccion tipo;
    private BigDecimal cantidad;
    private String descripcion;
    private LocalDate fecha;
    private Integer mes;
    private Integer anio;
}