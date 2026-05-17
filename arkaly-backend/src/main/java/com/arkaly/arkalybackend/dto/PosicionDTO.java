package com.arkaly.arkalybackend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

// DTO para una posición abierta del usuario en un activo concreto
@Data
public class PosicionDTO {
    private Integer id;
    private String simbolo;
    private String tipoActivo;
    private BigDecimal cantidadTotal;
    private BigDecimal precioMedioCompra;
    private BigDecimal totalInvertido;
    private BigDecimal valorActual;
    private BigDecimal pnl;             // ganancia/pérdida absoluta
    private BigDecimal pnlPorcentaje;   // ganancia/pérdida en porcentaje
    private LocalDate fechaApertura;
}