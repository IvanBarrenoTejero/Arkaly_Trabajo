package com.arkaly.arkalybackend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

// DTO para los movimientos de cartera (compras y ventas de activos)
@Data
public class CarteraDTO {
    private Integer id;
    private String simbolo;
    private String tipoActivo;
    private String tipoOperacion;
    private BigDecimal cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal precioTotal;
    private LocalDate fechaOperacion;
    private String notas;
}