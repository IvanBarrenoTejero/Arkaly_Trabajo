package com.arkaly.desktop.controllers;

import static com.arkaly.desktop.utils.Estilos.*;

import com.arkaly.desktop.services.InversionService;
import com.arkaly.desktop.utils.AsyncTask;
import com.arkaly.desktop.utils.InversionesUIHelper;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;

import java.util.*;

public class InversionesCarteraController {

    @FXML private VBox root;
    @FXML private VBox panelGrafica;
    @FXML private VBox listaPosiciones;
    @FXML private HBox filtrosPeriodo;
    @FXML private ComboBox<String> selectorTipoCartera;
    @FXML private Label lblValorCartera;
    @FXML private Label lblCambioCartera;

    private LineChart<Number, Number> chartCartera;
    private XYChart.Series<Number, Number> serieCartera;
    private Button btnPeriodoActivo;
    private Set<String> simbolosConPosicion = new HashSet<>();
    private String tipoSeleccionado = "TODOS";
    private String periodoSeleccionado = "1M";

    @FXML public void initialize() {
        selectorTipoCartera.getItems().addAll("Todos", "Cripto", "Acciones", "Fondos");
        selectorTipoCartera.getSelectionModel().selectFirst();
        selectorTipoCartera.setOnAction(e -> {
            tipoSeleccionado = mapTipo(selectorTipoCartera.getValue());
            cargarPosiciones();
            cargarGraficaCartera();
        });
        crearChartCartera();
        crearBotonesPeriodo();
        cargarPosiciones();
        cargarGraficaCartera();
    }

    private String mapTipo(String sel) {
        return switch (sel) { case "Cripto" -> "CRIPTO"; case "Acciones" -> "ACCION"; case "Fondos" -> "FONDO"; default -> "TODOS"; };
    }

    // GRÁFICA

    private void crearChartCartera() {
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setVisible(false); xAxis.setTickMarkVisible(false); xAxis.setTickLabelsVisible(false);
        yAxis.setTickMarkVisible(false);
        yAxis.setStyle("-fx-tick-label-fill: #a0aec0; -fx-font-size: 10px;");
        xAxis.setAutoRanging(true); yAxis.setAutoRanging(true);

        chartCartera = new LineChart<>(xAxis, yAxis);
        chartCartera.setLegendVisible(false); chartCartera.setAnimated(false);
        chartCartera.setCreateSymbols(false); chartCartera.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(chartCartera, Priority.ALWAYS);

        serieCartera = new XYChart.Series<>();
        chartCartera.getData().add(serieCartera);
        chartCartera.sceneProperty().addListener((obs, o, sc) -> {
            if (sc != null) Platform.runLater(() -> InversionesUIHelper.limpiarFondoChart(chartCartera));
        });
        panelGrafica.getChildren().add(chartCartera);
    }

    private void crearBotonesPeriodo() {
        for (String p : new String[]{"1D", "1S", "1M", "3M", "6M", "1A", "5A", "Máx"}) {
            Button btn = new Button(p);
            btn.setStyle(PERIODO_INACTIVO);
            btn.setOnMouseClicked(e -> {
                if (btnPeriodoActivo != null) btnPeriodoActivo.setStyle(PERIODO_INACTIVO);
                btn.setStyle(PERIODO_ACTIVO); btnPeriodoActivo = btn;
                periodoSeleccionado = p; cargarGraficaCartera();
            });
            filtrosPeriodo.getChildren().add(btn);
            if (p.equals("1M")) { btn.setStyle(PERIODO_ACTIVO); btnPeriodoActivo = btn; }
        }
    }

    private void cargarGraficaCartera() {
        String[] yf = InversionesUIHelper.periodoToYFinance(periodoSeleccionado);

        serieCartera.getData().clear();
        lblValorCartera.setText("Cargando...");
        lblCambioCartera.setText("");
        lblCambioCartera.setStyle("-fx-font-size: 13px; -fx-text-fill: #a0aec0;");

        final TreeMap<String, Double> mapa = new TreeMap<>();
        final double[] totalInvertido = {0};
        final List<JsonNode> listaPos = new ArrayList<>();

        AsyncTask.ejecutar(
                () -> {
                    JsonNode posiciones = InversionService.getPosiciones(tipoSeleccionado);
                    if (!posiciones.isArray() || posiciones.isEmpty()) return;
                    posiciones.forEach(listaPos::add);

                    for (JsonNode p : posiciones) {
                        String sim = p.path("simbolo").asText();
                        String tip = p.path("tipoActivo").asText();
                        double cant = p.path("cantidadTotal").asDouble();
                        String fechaAp = p.path("fechaApertura").asText("");
                        totalInvertido[0] += p.path("totalInvertido").asDouble();

                        try {
                            JsonNode puntos = InversionService.getHistorico(sim, tip, yf[0], yf[1]).get("puntos");
                            if (puntos != null && puntos.isArray()) {
                                for (JsonNode pt : puntos) {
                                    String fecha = pt.path("fecha").asText("");
                                    if (fecha.isEmpty()) continue;
                                    if (!fechaAp.isEmpty() && fecha.compareTo(fechaAp) < 0) continue;
                                    mapa.merge(fecha, pt.path("precio").asDouble() * cant, Double::sum);
                                }
                            }
                        } catch (Exception ignored) {}
                    }
                },
                () -> {
                    if (listaPos.isEmpty() || mapa.isEmpty()) {
                        serieCartera.getData().clear();
                        lblValorCartera.setText(listaPos.isEmpty() ? "0.00 €" : "—");
                        lblCambioCartera.setText(listaPos.isEmpty() ? "Sin posiciones" : "Sin datos históricos");
                        lblCambioCartera.setStyle("-fx-font-size: 13px; -fx-text-fill: #a0aec0;");
                        return;
                    }

                    actualizarBotonesPeriodo(listaPos);
                    List<Double> vals = new ArrayList<>(mapa.values());
                    serieCartera.getData().clear();

                    if (vals.size() <= 1) {
                        lblValorCartera.setText(String.format("%.2f €", vals.get(0)));
                        lblCambioCartera.setText("— +0.00%");
                        lblCambioCartera.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #a0aec0;");
                        InversionesUIHelper.limpiarFondoChart(chartCartera);
                        return;
                    }

                    for (int i = 0; i < vals.size(); i++)
                        serieCartera.getData().add(new XYChart.Data<>(i, vals.get(i)));
                    ajustarEjeY(vals);

                    double ultimo = vals.get(vals.size() - 1);
                    lblValorCartera.setText(String.format("%.2f €", ultimo));

                    if (totalInvertido[0] > 0) {
                        double cambio = ((ultimo - totalInvertido[0]) / totalInvertido[0]) * 100;
                        String color = cambio >= 0 ? "#48bb78" : "#fc8181";
                        lblCambioCartera.setText(String.format("%s %s%.2f%%", cambio >= 0 ? "▲" : "▼", cambio >= 0 ? "+" : "", cambio));
                        lblCambioCartera.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
                        chartCartera.applyCss(); chartCartera.layout();
                        javafx.scene.Node linea = chartCartera.lookup(".chart-series-line");
                        if (linea != null) linea.setStyle("-fx-stroke: " + color + "; -fx-stroke-width: 2px;");
                    }
                    InversionesUIHelper.limpiarFondoChart(chartCartera);
                }
        );
    }

    private void ajustarEjeY(List<Double> vals) {
        double min = vals.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double max = vals.stream().mapToDouble(Double::doubleValue).max().orElse(1);
        double margen = (max - min) * 0.1; if (margen == 0) margen = max * 0.01;
        NumberAxis yAxis = (NumberAxis) chartCartera.getYAxis();
        yAxis.setAutoRanging(false); yAxis.setLowerBound(min - margen);
        yAxis.setUpperBound(max + margen); yAxis.setTickUnit((max - min + 2 * margen) / 5);
    }

    // POSICIONES

    private void cargarPosiciones() {
        listaPosiciones.getChildren().clear();
        Label cargando = new Label("⏳ Cargando posiciones..."); cargando.setStyle(FB_CARGANDO);
        listaPosiciones.getChildren().add(cargando);

        final JsonNode[] posiciones = {null};
        AsyncTask.ejecutar(
                () -> posiciones[0] = InversionService.getPosiciones(tipoSeleccionado),
                () -> {
                    Set<String> posSet = new HashSet<>();
                    if (posiciones[0] != null && posiciones[0].isArray())
                        for (JsonNode p : posiciones[0]) posSet.add(p.path("simbolo").asText());
                    simbolosConPosicion = posSet;
                    listaPosiciones.getChildren().clear();

                    if (posiciones[0] == null || !posiciones[0].isArray() || posiciones[0].isEmpty()) {
                        listaPosiciones.getChildren().add(new Label("No tienes posiciones abiertas") {{ setStyle("-fx-text-fill: #a0aec0; -fx-font-size: 13px;"); }});
                        return;
                    }
                    for (JsonNode p : posiciones[0]) listaPosiciones.getChildren().add(crearFilaPosicion(p));
                }
        );
    }

    private VBox crearFilaPosicion(JsonNode p) {
        String simbolo = p.path("simbolo").asText();
        String tipo = p.path("tipoActivo").asText();
        double cantidad = p.path("cantidadTotal").asDouble();
        double precioMedio = p.path("precioMedioCompra").asDouble();
        double totalInvertido = p.path("totalInvertido").asDouble();

        StackPane logo = new StackPane();
        logo.setPrefSize(36, 36); logo.setMinSize(36, 36); logo.setMaxSize(36, 36);
        logo.setStyle("-fx-background-color: #edf2f7; -fx-background-radius: 18px;");
        logo.getChildren().add(new Label(InversionesUIHelper.emojiTipo(tipo)) {{ setStyle("-fx-font-size: 16px;"); }});

        VBox info = new VBox(2);
        info.getChildren().addAll(
                new Label(simbolo) {{ setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2d3748;"); }},
                new Label(tipo) {{ setStyle("-fx-font-size: 10px; -fx-text-fill: #a0aec0;"); }});

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblInvertido = new Label(String.format("%.2f €", totalInvertido));
        lblInvertido.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
        Label lblPnl = new Label("...");
        lblPnl.setStyle("-fx-font-size: 11px; -fx-text-fill: #a0aec0;");
        VBox valores = new VBox(2, lblInvertido, lblPnl); valores.setAlignment(Pos.CENTER_RIGHT);

        HBox filaTop = new HBox(10, logo, info, spacer, valores); filaTop.setAlignment(Pos.CENTER_LEFT);

        Label lblDetalle = new Label(String.format("Cantidad: %.4f  ·  Precio medio: %.2f €", cantidad, precioMedio));
        lblDetalle.setStyle("-fx-font-size: 10px; -fx-text-fill: #a0aec0;");

        VBox tarjeta = new VBox(6, filaTop, lblDetalle);
        tarjeta.setPadding(new Insets(12)); tarjeta.setStyle(FILA_NORMAL);
        tarjeta.setOnMouseEntered(e -> tarjeta.setStyle(FILA_HOVER));
        tarjeta.setOnMouseExited(e -> tarjeta.setStyle(FILA_NORMAL));
        tarjeta.setOnMouseClicked(e -> { if (e.getButton() == MouseButton.PRIMARY) InversionesUIHelper.abrirDetalle(p, tipo, simbolosConPosicion, root); });

        cargarPnl(p, lblPnl, lblInvertido);
        return tarjeta;
    }

    private void cargarPnl(JsonNode p, Label lblPnl, Label lblValor) {
        double valorActual = p.path("valorActual").asDouble(0);
        double pnl = p.path("pnl").asDouble(0);
        double pnlPct = p.path("pnlPorcentaje").asDouble(0);

        if (valorActual > 0) {
            lblValor.setText(String.format("%.2f €", valorActual));
            boolean pos = pnl >= 0; String s = pos ? "+" : ""; String color = pos ? "#48bb78" : "#fc8181";
            lblPnl.setText(String.format("%s %s%.2f €  (%s%.1f%%)", pos ? "▲" : "▼", s, pnl, s, pnlPct));
            lblPnl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        } else {
            lblPnl.setText("Sin precio"); lblPnl.setStyle("-fx-font-size: 11px; -fx-text-fill: #a0aec0;");
        }
    }

    // PERIODO BOTONES

    private void actualizarBotonesPeriodo(List<JsonNode> posiciones) {
        java.time.LocalDate fechaMin = posiciones.stream()
                .map(p -> { try { return java.time.LocalDate.parse(p.path("fechaApertura").asText()); } catch (Exception e) { return java.time.LocalDate.now(); } })
                .min(java.util.Comparator.naturalOrder()).orElse(java.time.LocalDate.now());

        Platform.runLater(() -> {
            String mejor = null;
            for (javafx.scene.Node node : filtrosPeriodo.getChildren()) {
                if (node instanceof Button btn) {
                    java.time.LocalDate inicio = java.time.LocalDate.now().minusDays(diasPeriodo(btn.getText()));
                    boolean sinDatos = fechaMin.isAfter(inicio.plusDays(1));
                    btn.setDisable(sinDatos); btn.setOpacity(sinDatos ? 0.35 : 1.0);
                    btn.setTooltip(sinDatos ? new Tooltip("Sin datos para este período") : null);
                    if (!sinDatos && mejor == null) mejor = btn.getText();
                }
            }
            if (mejor != null) {
                boolean actualInvalido = java.time.LocalDate.now().minusDays(diasPeriodo(periodoSeleccionado)).isBefore(fechaMin);
                if (actualInvalido) {
                    periodoSeleccionado = mejor;
                    for (javafx.scene.Node node : filtrosPeriodo.getChildren()) {
                        if (node instanceof Button btn) {
                            if (btn.getText().equals(mejor)) { btn.setStyle(PERIODO_ACTIVO); btnPeriodoActivo = btn; }
                            else btn.setStyle(PERIODO_INACTIVO);
                        }
                    }
                    cargarGraficaCartera();
                }
            }
        });
    }

    private long diasPeriodo(String p) {
        return switch (p) { case "1D" -> 1L; case "1S" -> 7L; case "1M" -> 30L; case "3M" -> 90L;
            case "6M" -> 180L; case "1A" -> 365L; case "5A" -> 1825L; default -> 36500L; };
    }

    // NAVEGACIÓN

    @FXML public void mostrarGeneral()   { navegar("Inversiones_General.fxml"); }
    @FXML public void mostrarOrdenes()   { navegar("Inversiones_Ordenes.fxml"); }
    @FXML public void mostrarHistorial() { navegar("Inversiones_Historial.fxml"); }
    @FXML public void mostrarAlertas()   { navegar("Inversiones_Alertas.fxml"); }

    private void navegar(String fxml) {
        try {
            javafx.scene.Node node = new javafx.fxml.FXMLLoader(getClass().getResource("/com/arkaly/desktop/" + fxml)).load();
            StackPane parent = (StackPane) root.getScene().lookup("#contenidoPanel");
            if (parent != null) parent.getChildren().setAll(node);
        } catch (Exception e) { e.printStackTrace(); }
    }
}