package com.arkaly.arkalybackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Map;

// DTO para el resumen patrimonial completo del usuario (liquidez + inversiones)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResumenPatrimonialDTO {
    private BigDecimal liquidez;                   // ingresos - gastos de transacciones
    private BigDecimal valorCartera;               // valor actual de las posiciones
    private BigDecimal totalInvertido;             // capital invertido total
    private BigDecimal pnlInversiones;             // ganancia/pérdida no realizada
    private BigDecimal pnlPorcentaje;              // % de variación de la cartera
    private BigDecimal patrimonioNeto;             // liquidez + valorCartera
    private BigDecimal gananciaRealizadaMes;       // beneficio de ventas del mes actual
    private int numeroPosiciones;
    private Map<String, BigDecimal> valorPorTipo;  // valor actual por tipo (CRIPTO, ACCION...)
}