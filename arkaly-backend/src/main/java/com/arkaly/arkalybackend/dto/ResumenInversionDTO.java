package com.arkaly.arkalybackend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

//DTO para el resumen global de la cartera de inversiones del usuario.
@Data
public class ResumenInversionDTO {
    private BigDecimal totalInvertido;
    private BigDecimal valorActual;
    private BigDecimal porcentajeVariacion;
    private Map<String, BigDecimal> totalPorTipo;       // capital invertido por tipo de activo
    private Map<String, BigDecimal> valorActualPorTipo; // valor actual por tipo de activo
    private Map<String, BigDecimal> porcentajePorTipo;  // % del portfolio por tipo de activo
    private int numeroPosiciones;
}