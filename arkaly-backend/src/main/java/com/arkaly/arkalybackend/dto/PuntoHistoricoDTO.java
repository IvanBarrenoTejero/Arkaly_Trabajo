package com.arkaly.arkalybackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

// DTO para un punto de datos histórico de precio de un activo (fecha + precio). */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PuntoHistoricoDTO {
    private String fecha;
    private BigDecimal precio;
}