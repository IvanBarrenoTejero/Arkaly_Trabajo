package com.arkaly.arkalybackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * DTO para la proyección financiera del usuario basada en la regla 50/30/20.
 * Incluye también el estado actual de la cartera de inversiones.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProyeccionDTO {
    private String reglaFinanciera;           // ej: "50/30/20"
    private BigDecimal mediaIngresosMensual;
    private BigDecimal mediaGastosMensual;
    private BigDecimal ahorroSugerido;
    private BigDecimal ahorroReal;
    private BigDecimal gastoNecesarios;
    private BigDecimal gastoOcio;
    private String mensaje;
    private BigDecimal valorCartera;          // valor actual de todas las posiciones
    private BigDecimal totalInvertido;        // capital invertido total
    private BigDecimal pnlInversiones;        // ganancia/pérdida no realizada
    private BigDecimal pnlPorcentaje;         // % de variación de la cartera
    private BigDecimal patrimonioTotal;       // ahorroReal + valorCartera
    private int numeroPosiciones;

    /**
     * Constructor sin campos de inversiones para mantener compatibilidad.
     */
    public ProyeccionDTO(String reglaFinanciera, BigDecimal mediaIngresosMensual,
                         BigDecimal mediaGastosMensual, BigDecimal ahorroSugerido,
                         BigDecimal ahorroReal, BigDecimal gastoNecesarios,
                         BigDecimal gastoOcio, String mensaje) {
        this.reglaFinanciera = reglaFinanciera;
        this.mediaIngresosMensual = mediaIngresosMensual;
        this.mediaGastosMensual = mediaGastosMensual;
        this.ahorroSugerido = ahorroSugerido;
        this.ahorroReal = ahorroReal;
        this.gastoNecesarios = gastoNecesarios;
        this.gastoOcio = gastoOcio;
        this.mensaje = mensaje;
    }
}