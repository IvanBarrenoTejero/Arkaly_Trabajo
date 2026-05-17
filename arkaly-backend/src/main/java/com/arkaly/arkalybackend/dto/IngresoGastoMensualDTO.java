package com.arkaly.arkalybackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * DTO para el resumen mensual de ingresos y gastos.
 * Incluye también el impacto de inversiones realizadas ese mes.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IngresoGastoMensualDTO {
    private int mes;
    private int anio;
    private String etiquetaMes;
    private BigDecimal totalIngresos;
    private BigDecimal totalGastos;
    private BigDecimal balance;
    private BigDecimal gananciaInversiones;    // beneficio realizado por ventas ese mes
    private BigDecimal perdidaInversiones;     // pérdida realizada por ventas ese mes
    private BigDecimal balanceConInversiones;  // balance + ganancia - pérdida

    /**
     * Constructor sin campos de inversiones para mantener compatibilidad.
     * Los campos de inversiones se inicializan a cero.
     */
    public IngresoGastoMensualDTO(int mes, int anio, String etiquetaMes,
                                  BigDecimal totalIngresos, BigDecimal totalGastos,
                                  BigDecimal balance) {
        this.mes = mes;
        this.anio = anio;
        this.etiquetaMes = etiquetaMes;
        this.totalIngresos = totalIngresos;
        this.totalGastos = totalGastos;
        this.balance = balance;
        this.gananciaInversiones = BigDecimal.ZERO;
        this.perdidaInversiones = BigDecimal.ZERO;
        this.balanceConInversiones = balance;
    }
}