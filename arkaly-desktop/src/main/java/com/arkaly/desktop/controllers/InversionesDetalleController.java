package com.arkaly.desktop.controllers;

import static com.arkaly.desktop.utils.Estilos.*;

import com.arkaly.desktop.services.InversionService;
import com.arkaly.desktop.utils.AsyncTask;
import com.arkaly.desktop.utils.InversionesUIHelper;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

public class InversionesDetalleController {

    @FXML private Button btnVolver;
    @FXML private Label lblTituloActivo, lblPrecioActual, lblCambio;
    @FXML private LineChart<Number, Number> chartGrande;
    @FXML private ToggleButton btn1D, btn1S, btn1M, btn3M, btn6M, btn1A, btn5A, btnMax;
    @FXML private ToggleGroup grupoPeriodo, grupoOperacion;
    @FXML private Label lblSimboloInfo, lblTipoInfo, lblNombreInfo;
    @FXML private ToggleButton btnCompra, btnVenta;
    @FXML private TextField campoCantidad, campoPrecio, campoNotas;
    @FXML private Label lblResumen, lblFeedback;
    @FXML private Button btnConfirmar;

    private String simbolo, nombre, tipo;
    private XYChart.Series<Number, Number> serie;

    @FXML public void initialize() {
        serie = new XYChart.Series<>();
        chartGrande.getData().add(serie);
        chartGrande.sceneProperty().addListener((obs, o, sc) -> {
            if (sc != null) Platform.runLater(this::limpiarFondoChart);
        });

        btnVolver.setOnMouseEntered(e -> btnVolver.setStyle(btnVolver.getStyle().replace("transparent", "#ebf8ff")));
        btnVolver.setOnMouseExited(e -> btnVolver.setStyle(btnVolver.getStyle().replace("#ebf8ff", "transparent")));
        btnConfirmar.setOnMouseEntered(e -> btnConfirmar.setStyle(btnConfirmar.getStyle().replace("#4a90e2", "#2b6cb0")));
        btnConfirmar.setOnMouseExited(e -> btnConfirmar.setStyle(btnConfirmar.getStyle().replace("#2b6cb0", "#4a90e2")));

        grupoPeriodo.selectedToggleProperty().addListener((obs, oldT, newT) ->
                grupoPeriodo.getToggles().forEach(t -> ((ToggleButton) t).setStyle(t == newT ? PERIODO_ACTIVO : PERIODO_INACTIVO)));
        btn1M.setStyle(PERIODO_ACTIVO);
    }

    public void setActivo(String simbolo, String nombre, String tipo) {
        this.simbolo = simbolo; this.nombre = nombre; this.tipo = tipo;
        String emoji = InversionesUIHelper.emojiTipo(tipo);
        lblTituloActivo.setText(emoji + "  " + nombre + "  ·  " + simbolo);
        lblSimboloInfo.setText("Símbolo:  " + simbolo);
        lblTipoInfo.setText("Tipo:  " + tipo);
        lblNombreInfo.setText("Nombre:  " + nombre);
        cargarHistorico("1M");
    }

    @FXML private void onVolver() {
        try {
            javafx.scene.Node node = new javafx.fxml.FXMLLoader(getClass().getResource("/com/arkaly/desktop/Inversiones_Ordenes.fxml")).load();
            StackPane panel = (StackPane) btnVolver.getScene().lookup("#contenidoPanel");
            if (panel != null) panel.getChildren().setAll(node);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void onFiltroPeriodo(javafx.event.ActionEvent event) {
        cargarHistorico(((ToggleButton) event.getSource()).getText());
    }

    @FXML private void onCambioOperacion() {
        boolean esCompra = btnCompra.isSelected();
        String base = "-fx-background-radius: 8px; -fx-font-size: 12px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 7 16 7 16;";
        btnCompra.setStyle(base + (esCompra ? "-fx-background-color: #48bb78; -fx-text-fill: white;" : "-fx-background-color: #e2e8f0; -fx-text-fill: #718096;"));
        btnVenta.setStyle(base + (!esCompra ? "-fx-background-color: #fc8181; -fx-text-fill: white;" : "-fx-background-color: #e2e8f0; -fx-text-fill: #718096;"));
        actualizarResumen();
    }

    @FXML private void onCamposCambian() { actualizarResumen(); }

    @FXML private void onConfirmar() {
        String cantStr = campoCantidad.getText().trim().replace(",", ".");
        String precStr = campoPrecio.getText().trim().replace(",", ".");
        String notasStr = campoNotas.getText().trim();
        if (cantStr.isEmpty() || precStr.isEmpty()) { mostrarFeedback("⚠️ Completa cantidad y precio.", "#ed8936"); return; }
        try { Double.parseDouble(cantStr); Double.parseDouble(precStr); }
        catch (NumberFormatException ex) { mostrarFeedback("⚠️ Valores no válidos.", "#fc8181"); return; }

        String tipoOp = btnCompra.isSelected() ? "COMPRA" : "VENTA";
        btnConfirmar.setDisable(true);
        mostrarFeedback("⏳ Registrando...", "#a0aec0");

        AsyncTask.ejecutar(
                () -> InversionService.registrarOperacion(simbolo, tipo, tipoOp, cantStr, precStr, notasStr),
                () -> {
                    mostrarFeedback("✅ Operación registrada.", "#48bb78");
                    campoCantidad.clear(); campoPrecio.clear(); campoNotas.clear();
                    lblResumen.setText(""); btnConfirmar.setDisable(false);
                }
        );
    }

    private void cargarHistorico(String periodo) {
        lblPrecioActual.setText("⏳"); lblCambio.setText("");
        String[] yf = InversionesUIHelper.periodoToYFinance(periodo);

        final JsonNode[] puntos = {null};
        AsyncTask.ejecutar(
                () -> puntos[0] = InversionService.getHistorico(simbolo, tipo, yf[0], yf[1]).path("puntos"),
                () -> {
                    serie.getData().clear();
                    if (puntos[0] != null && puntos[0].isArray() && puntos[0].size() > 1) {
                        for (int i = 0; i < puntos[0].size(); i++)
                            serie.getData().add(new XYChart.Data<>(i, puntos[0].get(i).path("precio").asDouble()));
                        double primero = puntos[0].get(0).path("precio").asDouble();
                        double ultimo = puntos[0].get(puntos[0].size() - 1).path("precio").asDouble();
                        lblPrecioActual.setText(String.format("%.4f €", ultimo));
                        if (primero > 0) {
                            double pct = ((ultimo - primero) / primero) * 100;
                            boolean sube = pct >= 0;
                            String color = sube ? "#48bb78" : "#fc8181";
                            lblCambio.setText(String.format("%s %s%.2f%% (%s)", sube ? "▲" : "▼", sube ? "+" : "", pct, periodo));
                            lblCambio.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
                            Platform.runLater(() -> {
                                limpiarFondoChart();
                                if (serie.getNode() != null) {
                                    javafx.scene.Node linea = serie.getNode().lookup(".chart-series-line");
                                    if (linea != null) linea.setStyle("-fx-stroke: " + color + "; -fx-stroke-width: 2px;");
                                }
                            });
                        }
                    } else { lblPrecioActual.setText("Sin datos"); lblCambio.setText(""); }
                }
        );
    }

    private void actualizarResumen() {
        try {
            double cant = Double.parseDouble(campoCantidad.getText().replace(",", "."));
            double prec = Double.parseDouble(campoPrecio.getText().replace(",", "."));
            lblResumen.setText(String.format("Estimado: %s de %.4f %s × %.2f € = %.2f €",
                    btnCompra.isSelected() ? "Compra" : "Venta", cant, simbolo, prec, cant * prec));
        } catch (NumberFormatException ex) { lblResumen.setText(""); }
    }

    private void mostrarFeedback(String msg, String color) {
        lblFeedback.setText(msg); lblFeedback.setStyle("-fx-font-size: 12px; -fx-text-fill: " + color + ";");
    }

    private void limpiarFondoChart() { InversionesUIHelper.limpiarFondoChart(chartGrande); }
}