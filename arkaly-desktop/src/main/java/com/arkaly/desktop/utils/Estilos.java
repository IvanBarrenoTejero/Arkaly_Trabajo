package com.arkaly.desktop.utils;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class Estilos {
    private Estilos() {}

    // Tarjetas
    public static final String TARJETA_NORMAL =
            "-fx-background-color: white; -fx-background-radius: 10px; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2); -fx-cursor: hand;";
    public static final String TARJETA_HOVER =
            "-fx-background-color: #ebf8ff; -fx-background-radius: 10px; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 3); -fx-cursor: hand;";

    // Cards detalle
    public static final String CARD =
            "-fx-background-color: white; -fx-background-radius: 14px; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 10, 0, 0, 3);";

    // Campos de texto
    public static final String CAMPO =
            "-fx-background-color: #f7fafc; -fx-border-color: #e2e8f0; " +
                    "-fx-border-radius: 8px; -fx-background-radius: 8px; " +
                    "-fx-font-size: 13px; -fx-padding: 8 12 8 12;";
    public static final String CAMPO_BLANCO =
            "-fx-background-color: white; -fx-border-color: #e2e8f0; " +
                    "-fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 8 12 8 12;";

    // Filas
    public static final String FILA_NORMAL =
            "-fx-background-color: #f7fafc; -fx-background-radius: 10px; " +
                    "-fx-border-color: #e2e8f0; -fx-border-radius: 10px;";
    public static final String FILA_HOVER =
            "-fx-background-color: #ebf8ff; -fx-background-radius: 10px; " +
                    "-fx-border-color: #bee3f8; -fx-border-radius: 10px;";

    // Botones
    public static final String BTN_PRIMARIO =
            "-fx-background-color: #4a90e2; -fx-text-fill: white; " +
                    "-fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 8px; " +
                    "-fx-cursor: hand; -fx-padding: 6 14 6 14;";
    public static final String BTN_VERDE =
            "-fx-background-color: #48bb78; -fx-text-fill: white; " +
                    "-fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 8px; " +
                    "-fx-cursor: hand; -fx-padding: 6 14 6 14;";
    public static final String BTN_NARANJA =
            "-fx-background-color: #ed8936; -fx-text-fill: white; " +
                    "-fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 8px; " +
                    "-fx-cursor: hand; -fx-padding: 8 20 8 20;";
    public static final String BTN_SECUNDARIO =
            "-fx-background-color: #e2e8f0; -fx-text-fill: #4a5568; " +
                    "-fx-font-size: 12px; -fx-background-radius: 8px; -fx-cursor: hand; -fx-padding: 8 20 8 20;";
    public static final String BTN_ELIMINAR =
            "-fx-background-color: transparent; -fx-text-fill: #fc8181; " +
                    "-fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 4 8 4 8;";
    public static final String BTN_TOGGLE_COMPRA =
            "-fx-background-radius: 8px; -fx-font-size: 12px; -fx-font-weight: bold; " +
                    "-fx-cursor: hand; -fx-padding: 7 16 7 16; -fx-background-color: #48bb78; -fx-text-fill: white;";
    public static final String BTN_TOGGLE_INACTIVO =
            "-fx-background-radius: 8px; -fx-font-size: 12px; -fx-font-weight: bold; " +
                    "-fx-cursor: hand; -fx-padding: 7 16 7 16; -fx-background-color: #e2e8f0; -fx-text-fill: #718096;";
    public static final String BTN_TOGGLE_VENTA =
            "-fx-background-radius: 8px; -fx-font-size: 12px; -fx-font-weight: bold; " +
                    "-fx-cursor: hand; -fx-padding: 7 16 7 16; -fx-background-color: #fc8181; -fx-text-fill: white;";

    // Feedback
    public static final String FB_OK = "-fx-font-size: 12px; -fx-text-fill: #48bb78;";
    public static final String FB_ERROR = "-fx-font-size: 12px; -fx-text-fill: #fc8181;";
    public static final String FB_WARN = "-fx-font-size: 12px; -fx-text-fill: #ed8936;";
    public static final String FB_CARGANDO = "-fx-font-size: 12px; -fx-text-fill: #a0aec0;";
    public static final String FB_OK_SM = "-fx-font-size: 11px; -fx-text-fill: #48bb78;";
    public static final String FB_ERROR_SM = "-fx-font-size: 11px; -fx-text-fill: #fc8181;";
    public static final String FB_WARN_SM = "-fx-font-size: 11px; -fx-text-fill: #ed8936;";
    public static final String FB_CARGANDO_SM = "-fx-font-size: 11px; -fx-text-fill: #a0aec0;";

    // Títulos
    public static final String TITULO = "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2d3748;";
    public static final String SUBTITULO = "-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #4a5568;";

    // Toggles filtro
    public static final String TOGGLE_ACTIVO =
            "-fx-background-color: #4299e1; -fx-text-fill: white; -fx-background-radius: 6px; -fx-padding: 6 12 6 12;";
    public static final String TOGGLE_INACTIVO =
            "-fx-background-color: #e2e8f0; -fx-background-radius: 6px; -fx-padding: 6 12 6 12;";

    // Periodo (gráficas)
    public static final String PERIODO_ACTIVO =
            "-fx-background-color: #4a90e2; -fx-text-fill: white; " +
                    "-fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand; " +
                    "-fx-background-radius: 8px; -fx-padding: 5 10 5 10;";
    public static final String PERIODO_INACTIVO =
            "-fx-background-color: #edf2f7; -fx-text-fill: #718096; " +
                    "-fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand; " +
                    "-fx-background-radius: 8px; -fx-padding: 5 10 5 10;";

    // Volver
    public static final String BTN_VOLVER =
            "-fx-background-color: transparent; -fx-text-fill: #4a90e2; " +
                    "-fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand; " +
                    "-fx-border-color: #bee3f8; -fx-border-radius: 8px; " +
                    "-fx-background-radius: 8px; -fx-padding: 6 14 6 14;";

    // Scroll transparente
    public static final String SCROLL_TRANSPARENTE =
            "-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;";

    // Helpers
    public static void hoverFila(HBox fila) {
        fila.setOnMouseEntered(e -> fila.setStyle(FILA_HOVER));
        fila.setOnMouseExited(e -> fila.setStyle(FILA_NORMAL));
    }

    public static void hoverTarjeta(VBox tarjeta) {
        tarjeta.setOnMouseEntered(e -> tarjeta.setStyle(TARJETA_HOVER));
        tarjeta.setOnMouseExited(e -> tarjeta.setStyle(TARJETA_NORMAL));
    }
}