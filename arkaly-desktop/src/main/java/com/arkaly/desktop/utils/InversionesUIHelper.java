package com.arkaly.desktop.utils;

import static com.arkaly.desktop.utils.Estilos.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.Set;

public class InversionesUIHelper {

    private static final ObjectMapper mapper = new ObjectMapper();

    // ─── DETALLE COMPLETO ────────────────────────────────────────

    public static void abrirDetalle(JsonNode activo, String tipo,
                                    Set<String> simbolosConPosicion,
                                    javafx.scene.Node nodoRaiz) {
        String simbolo = activo.path("simbolo").asText();
        String nombre  = activo.path("nombre").asText("");
        if (nombre.isEmpty()) nombre = simbolo;

        BorderPane panel = new BorderPane();
        panel.setStyle("-fx-background-color: #f7fafc;");

        // ── Barra superior ──
        Button btnVolver = new Button("← Volver");
        btnVolver.setStyle(BTN_VOLVER);
        btnVolver.setOnMouseEntered(e ->
                btnVolver.setStyle(BTN_VOLVER.replace("transparent", "#ebf8ff")));
        btnVolver.setOnMouseExited(e ->
                btnVolver.setStyle(BTN_VOLVER));

        Label lblTitulo = new Label(emojiTipo(tipo) + "  " + nombre + "  ·  " + simbolo);
        lblTitulo.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");

        HBox topBar = new HBox(16, btnVolver, lblTitulo);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(16, 20, 12, 20));
        topBar.setStyle("-fx-background-color: white; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 2);");
        panel.setTop(topBar);

        // ── IZQUIERDA: gráfica grande ──
        VBox izquierda = crearPanelGrafica(simbolo, tipo);

        // ── DERECHA: info + invertir + alerta ──
        VBox derecha = crearPanelDerecho(simbolo, tipo, nombre, simbolosConPosicion);

        HBox centro = new HBox(16, izquierda, derecha);
        centro.setPadding(new Insets(16, 20, 20, 20));
        HBox.setHgrow(izquierda, Priority.ALWAYS);
        panel.setCenter(centro);

        StackPane contenidoPanel = (StackPane) nodoRaiz.getScene().lookup("#contenidoPanel");
        if (contenidoPanel != null) {
            javafx.scene.Node vistaOriginal = contenidoPanel.getChildren().get(0);
            contenidoPanel.getChildren().setAll(panel);
            btnVolver.setOnAction(e -> contenidoPanel.getChildren().setAll(vistaOriginal));
        }
    }

    // ── Panel izquierdo: gráfica ──

    private static VBox crearPanelGrafica(String simbolo, String tipo) {
        VBox izquierda = new VBox(14);
        izquierda.setPadding(new Insets(20, 12, 20, 20));
        izquierda.setStyle(CARD);
        izquierda.setMinWidth(480);

        Label lblPrecio = new Label("—");
        lblPrecio.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
        Label lblCambio = new Label("");
        lblCambio.setStyle("-fx-font-size: 13px;");

        HBox precioRow = new HBox(14, lblPrecio, lblCambio);
        precioRow.setAlignment(Pos.BASELINE_LEFT);

        LineChart<Number, Number> chart = crearChartGrande();
        XYChart.Series<Number, Number> serie = new XYChart.Series<>();
        chart.getData().add(serie);

        HBox filtros = crearFiltrosPeriodo(simbolo, tipo, serie, chart, lblPrecio, lblCambio);

        izquierda.getChildren().addAll(precioRow, filtros, chart);
        cargarHistoricoGrande(simbolo, tipo, "1M", serie, chart, lblPrecio, lblCambio);

        return izquierda;
    }

    private static LineChart<Number, Number> crearChartGrande() {
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setVisible(false); xAxis.setTickMarkVisible(false); xAxis.setTickLabelsVisible(false);
        yAxis.setTickMarkVisible(false);
        yAxis.setStyle("-fx-tick-label-fill: #a0aec0; -fx-font-size: 10px;");
        xAxis.setAutoRanging(true); yAxis.setAutoRanging(true);

        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setCreateSymbols(false);
        chart.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(chart, Priority.ALWAYS);

        chart.sceneProperty().addListener((obs, o, sc) -> {
            if (sc != null) Platform.runLater(() -> limpiarFondoChart(chart));
        });

        return chart;
    }

    private static HBox crearFiltrosPeriodo(String simbolo, String tipo,
                                            XYChart.Series<Number, Number> serie,
                                            LineChart<Number, Number> chart,
                                            Label lblPrecio, Label lblCambio) {
        String[] periodos = {"1D", "1S", "1M", "3M", "6M", "1A", "5A", "Máx"};
        HBox filtros = new HBox(8);
        filtros.setAlignment(Pos.CENTER_LEFT);
        final Button[] btnActivo = {null};

        for (String periodo : periodos) {
            Button btn = new Button(periodo);
            btn.setStyle(PERIODO_INACTIVO);
            btn.setOnMouseClicked(e -> {
                if (btnActivo[0] != null) btnActivo[0].setStyle(PERIODO_INACTIVO);
                btn.setStyle(PERIODO_ACTIVO);
                btnActivo[0] = btn;
                cargarHistoricoGrande(simbolo, tipo, periodo, serie, chart, lblPrecio, lblCambio);
            });
            filtros.getChildren().add(btn);
            if (periodo.equals("1M")) {
                btn.setStyle(PERIODO_ACTIVO);
                btnActivo[0] = btn;
            }
        }
        return filtros;
    }

    // ── Panel derecho: info + formulario + alerta ──

    private static VBox crearPanelDerecho(String simbolo, String tipo, String nombre,
                                          Set<String> simbolosConPosicion) {
        VBox derecha = new VBox(16);
        derecha.setPadding(new Insets(20, 20, 20, 12));
        derecha.setPrefWidth(280);
        derecha.setMinWidth(260);
        derecha.setMaxWidth(320);

        VBox infoCard = crearInfoCard(simbolo, tipo, nombre);
        VBox formCard = crearFormInversion(simbolo, tipo, simbolosConPosicion);
        VBox alertaCard = crearAlertaCard(simbolo, tipo);

        VBox contenido = new VBox(16, infoCard, formCard, alertaCard);

        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle(SCROLL_TRANSPARENTE);

        derecha.getChildren().add(scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        return derecha;
    }

    private static VBox crearInfoCard(String simbolo, String tipo, String nombre) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(16));
        card.setStyle(CARD);

        Label titulo = new Label("ℹ️  Información");
        titulo.setStyle(SUBTITULO);

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #e2e8f0;");

        card.getChildren().addAll(titulo, sep,
                infoFila("Símbolo", simbolo),
                infoFila("Tipo", tipo),
                infoFila("Nombre", nombre));
        return card;
    }

    private static VBox crearFormInversion(String simbolo, String tipo,
                                           Set<String> simbolosConPosicion) {
        VBox form = new VBox(12);
        form.setPadding(new Insets(18));
        form.setStyle(CARD);

        Label titulo = new Label("💰  Invertir");
        titulo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");

        ToggleGroup tgOp = new ToggleGroup();
        ToggleButton btnCompra = new ToggleButton("COMPRAR");
        btnCompra.setToggleGroup(tgOp);
        btnCompra.setSelected(true);
        btnCompra.setStyle(BTN_TOGGLE_COMPRA);

        HBox toggleRow = new HBox(8);
        toggleRow.setAlignment(Pos.CENTER_LEFT);
        toggleRow.getChildren().add(btnCompra);

        if (simbolosConPosicion.contains(simbolo)) {
            ToggleButton btnVenta = new ToggleButton("VENDER");
            btnVenta.setToggleGroup(tgOp);
            btnVenta.setStyle(BTN_TOGGLE_INACTIVO);

            tgOp.selectedToggleProperty().addListener((obs, oldT, newT) -> {
                if (newT == btnCompra) {
                    btnCompra.setStyle(BTN_TOGGLE_COMPRA);
                    btnVenta.setStyle(BTN_TOGGLE_INACTIVO);
                } else if (newT == btnVenta) {
                    btnVenta.setStyle(BTN_TOGGLE_VENTA);
                    btnCompra.setStyle(BTN_TOGGLE_INACTIVO);
                }
            });
            toggleRow.getChildren().add(btnVenta);

            // Cargar cantidad disponible
            AsyncTask.ejecutar(
                    () -> {},  // placeholder
                    () -> {}
            );
            new Thread(() -> {
                try {
                    String res = ApiClient.getAuth("/inversiones/posiciones");
                    JsonNode posiciones = mapper.readTree(res);
                    for (JsonNode p : posiciones) {
                        if (p.path("simbolo").asText().equals(simbolo)) {
                            double cantDisp = p.path("cantidadTotal").asDouble();
                            Platform.runLater(() -> {
                                Label lblDisp = new Label(
                                        String.format("Disponible para vender: %.4f %s", cantDisp, simbolo));
                                lblDisp.setStyle("-fx-font-size: 10px; -fx-text-fill: #718096;");
                                int idx = form.getChildren().indexOf(toggleRow);
                                if (idx >= 0) form.getChildren().add(idx + 1, lblDisp);
                            });
                            break;
                        }
                    }
                } catch (Exception ignored) {}
            }).start();
        }

        TextField campoCantidad = crearCampo("Ej: 0.5");
        TextField campoPrecio = crearCampo("Precio de compra/venta");
        TextField campoNotas = crearCampo("Observaciones...");

        Label lblResumen = new Label("");
        lblResumen.setStyle("-fx-font-size: 11px; -fx-text-fill: #718096;");
        lblResumen.setWrapText(true);
        lblResumen.setMaxWidth(Double.MAX_VALUE);

        Runnable actualizarResumen = () -> {
            try {
                double cant = Double.parseDouble(campoCantidad.getText().replace(",", "."));
                double prec = Double.parseDouble(campoPrecio.getText().replace(",", "."));
                String op = btnCompra.isSelected() ? "Compra" : "Venta";
                lblResumen.setText(String.format("%s: %.4f × %.2f€ = %.2f€", op, cant, prec, cant * prec));
            } catch (NumberFormatException ex) {
                lblResumen.setText("");
            }
        };
        campoCantidad.textProperty().addListener((obs, o, n) -> actualizarResumen.run());
        campoPrecio.textProperty().addListener((obs, o, n) -> actualizarResumen.run());
        tgOp.selectedToggleProperty().addListener((obs, o, n) -> actualizarResumen.run());

        Label lblFeedback = new Label("");

        Button btnConfirmar = new Button("Confirmar operación");
        btnConfirmar.setMaxWidth(Double.MAX_VALUE);
        btnConfirmar.setStyle(
                "-fx-background-color: #4a90e2; -fx-text-fill: white; " +
                        "-fx-font-size: 13px; -fx-font-weight: bold; " +
                        "-fx-background-radius: 10px; -fx-cursor: hand; -fx-padding: 10 0 10 0;");
        btnConfirmar.setOnMouseEntered(e ->
                btnConfirmar.setStyle(btnConfirmar.getStyle().replace("#4a90e2", "#2b6cb0")));
        btnConfirmar.setOnMouseExited(e ->
                btnConfirmar.setStyle(btnConfirmar.getStyle().replace("#2b6cb0", "#4a90e2")));

        btnConfirmar.setOnAction(e -> {
            String cantStr = campoCantidad.getText().trim().replace(",", ".");
            String precStr = campoPrecio.getText().trim().replace(",", ".");
            String notasStr = campoNotas.getText().trim();

            if (cantStr.isEmpty() || precStr.isEmpty()) {
                lblFeedback.setText("⚠️ Completa cantidad y precio.");
                lblFeedback.setStyle(FB_WARN);
                return;
            }
            try { Double.parseDouble(cantStr); Double.parseDouble(precStr); }
            catch (NumberFormatException ex) {
                lblFeedback.setText("⚠️ Cantidad o precio no válidos.");
                lblFeedback.setStyle(FB_ERROR);
                return;
            }

            String tipoOp = btnCompra.isSelected() ? "COMPRA" : "VENTA";
            btnConfirmar.setDisable(true);
            lblFeedback.setText("⏳ Registrando...");
            lblFeedback.setStyle(FB_CARGANDO);

            String json = String.format(
                    "{\"simbolo\":\"%s\",\"tipoActivo\":\"%s\",\"tipoOperacion\":\"%s\"," +
                            "\"cantidad\":\"%s\",\"precioUnitario\":\"%s\",\"notas\":\"%s\"}",
                    simbolo, tipo, tipoOp, cantStr, precStr, notasStr);

            AsyncTask.ejecutar(
                    () -> ApiClient.postAuth("/inversiones/operacion", json),
                    () -> {
                        lblFeedback.setText("✅ Operación registrada correctamente.");
                        lblFeedback.setStyle(FB_OK);
                        campoCantidad.clear(); campoPrecio.clear(); campoNotas.clear();
                        btnConfirmar.setDisable(false);
                    }
            );
        });

        Label lblCantTxt = labelCampo("Cantidad");
        Label lblPrecTxt = labelCampo("Precio unitario (€)");
        Label lblNotTxt = labelCampo("Notas (opcional)");

        form.getChildren().addAll(titulo, new Separator(), toggleRow,
                lblCantTxt, campoCantidad, lblPrecTxt, campoPrecio,
                lblNotTxt, campoNotas, lblResumen, btnConfirmar, lblFeedback);

        return form;
    }

    private static VBox crearAlertaCard(String simbolo, String tipo) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(16));
        card.setStyle(CARD);

        Label titulo = new Label("🔔  Crear alerta de precio");
        titulo.setStyle(SUBTITULO);

        ComboBox<String> comboCondicion = new ComboBox<>();
        comboCondicion.getItems().addAll("Mayor que", "Menor que");
        comboCondicion.getSelectionModel().selectFirst();

        TextField campoPrecio = new TextField();
        campoPrecio.setPromptText("Precio objetivo (€)");
        campoPrecio.setStyle(CAMPO);

        HBox campos = new HBox(8, comboCondicion, campoPrecio);
        campos.setAlignment(Pos.CENTER_LEFT);

        Label lblFeedback = new Label("");
        lblFeedback.setStyle("-fx-font-size: 11px;");

        Button btnCrear = new Button("Crear alerta");
        btnCrear.setStyle(BTN_NARANJA);

        btnCrear.setOnAction(ev -> {
            String precioAlerta = campoPrecio.getText().trim().replace(",", ".");
            if (precioAlerta.isEmpty()) {
                lblFeedback.setText("⚠️ Introduce un precio");
                lblFeedback.setStyle(FB_WARN_SM);
                return;
            }
            try { Double.parseDouble(precioAlerta); } catch (NumberFormatException ex) {
                lblFeedback.setText("⚠️ Precio no válido");
                lblFeedback.setStyle(FB_ERROR_SM);
                return;
            }

            String condicion = comboCondicion.getValue().equals("Mayor que") ? "MAYOR_QUE" : "MENOR_QUE";
            btnCrear.setDisable(true);
            lblFeedback.setText("⏳ Creando...");
            lblFeedback.setStyle(FB_CARGANDO_SM);

            String json = String.format(
                    "{\"simbolo\":\"%s\",\"tipoActivo\":\"%s\",\"condicion\":\"%s\",\"precioObjetivo\":\"%s\"}",
                    simbolo, tipo, condicion, precioAlerta);

            AsyncTask.ejecutar(
                    () -> ApiClient.postAuth("/alertas", json),
                    () -> {
                        lblFeedback.setText("✅ Alerta creada");
                        lblFeedback.setStyle(FB_OK_SM);
                        campoPrecio.clear();
                        btnCrear.setDisable(false);
                    }
            );
        });

        card.getChildren().addAll(titulo, new Separator(), campos, btnCrear, lblFeedback);
        return card;
    }

    //  GRÁFICA MINI

    public static LineChart<Number, Number> crearMiniGrafica() {
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setVisible(false); xAxis.setTickMarkVisible(false); xAxis.setTickLabelsVisible(false);
        yAxis.setVisible(false); yAxis.setTickMarkVisible(false); yAxis.setTickLabelsVisible(false);
        xAxis.setAutoRanging(true); yAxis.setAutoRanging(true);

        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setCreateSymbols(false);
        chart.setPrefSize(130, 50);
        chart.setMinSize(130, 50);
        chart.setMaxSize(130, 50);
        chart.setPadding(new Insets(-8, -8, -8, -8));
        chart.setStyle("-fx-background-color: transparent;");

        chart.sceneProperty().addListener((obs, o, newScene) -> {
            if (newScene != null) Platform.runLater(() -> limpiarFondoChart(chart));
        });

        return chart;
    }

    // CARGAR HISTÓRICO EN TARJETA

    public static void cargarHistoricoEnTarjeta(String simbolo, String tipo, String periodo,
                                                XYChart.Series<Number, Number> serie,
                                                Label pctLabel,
                                                LineChart<Number, Number> grafica) {
        String[] yf = periodoToYFinance(periodo);
        final String url = "/inversiones/historico/" + simbolo
                + "?tipo=" + tipo + "&period=" + yf[0] + "&interval=" + yf[1];

        final JsonNode[] puntos = {null};
        AsyncTask.ejecutar(
                () -> puntos[0] = mapper.readTree(ApiClient.getAuth(url)).path("puntos"),
                () -> {
                    if (puntos[0] != null && puntos[0].isArray() && puntos[0].size() > 1) {
                        aplicarDatosASerie(serie, puntos[0], grafica);
                        aplicarPorcentajeTarjeta(puntos[0], pctLabel, serie, grafica);
                    } else {
                        pctLabel.setText("sin datos");
                        pctLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #cbd5e0;");
                    }
                }
        );
    }

    private static void aplicarDatosASerie(XYChart.Series<Number, Number> serie,
                                           JsonNode puntos,
                                           LineChart<Number, Number> grafica) {
        serie.getData().clear();
        double min = Double.MAX_VALUE, max = Double.MIN_VALUE;
        for (int i = 0; i < puntos.size(); i++) {
            double val = puntos.get(i).path("precio").asDouble();
            serie.getData().add(new XYChart.Data<>(i, val));
            if (val < min) min = val;
            if (val > max) max = val;
        }
        ajustarEjeY(grafica, min, max, 0.1);
    }

    private static void aplicarPorcentajeTarjeta(JsonNode puntos, Label pctLabel,
                                                 XYChart.Series<Number, Number> serie,
                                                 LineChart<Number, Number> grafica) {
        double primero = puntos.get(0).path("precio").asDouble();
        double ultimo = puntos.get(puntos.size() - 1).path("precio").asDouble();
        if (primero <= 0) return;

        double pct = ((ultimo - primero) / primero) * 100;
        boolean sube = pct >= 0;
        String color = sube ? "#48bb78" : "#fc8181";

        pctLabel.setText(String.format("%s %s%.2f%%",
                sube ? "▲" : "▼", sube ? "+" : "", pct));
        pctLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        Platform.runLater(() -> {
            limpiarFondoChart(grafica);
            if (serie.getNode() != null) {
                javafx.scene.Node linea = serie.getNode().lookup(".chart-series-line");
                if (linea != null)
                    linea.setStyle("-fx-stroke: " + color + "; -fx-stroke-width: 1.5px;");
            }
        });
    }

    // HISTÓRICO GRANDE

    public static void cargarHistoricoGrande(String simbolo, String tipo, String periodo,
                                             XYChart.Series<Number, Number> serie,
                                             LineChart<Number, Number> chart,
                                             Label lblPrecio, Label lblCambio) {
        String[] yf = periodoToYFinance(periodo);
        String endpoint = "/inversiones/historico/" + simbolo
                + "?tipo=" + tipo + "&period=" + yf[0] + "&interval=" + yf[1];

        final JsonNode[] puntos = {null};
        AsyncTask.ejecutar(
                () -> puntos[0] = mapper.readTree(ApiClient.getAuth(endpoint)).get("puntos"),
                () -> {
                    serie.getData().clear();
                    if (puntos[0] != null && puntos[0].isArray() && puntos[0].size() > 0) {
                        double min = Double.MAX_VALUE, max = Double.MIN_VALUE;
                        for (int i = 0; i < puntos[0].size(); i++) {
                            double val = puntos[0].get(i).path("precio").doubleValue();
                            serie.getData().add(new XYChart.Data<>(i, val));
                            if (val < min) min = val;
                            if (val > max) max = val;
                        }
                        ajustarEjeY(chart, min, max, 0.05);

                        double primero = puntos[0].get(0).path("precio").doubleValue();
                        double ultimo = puntos[0].get(puntos[0].size() - 1).path("precio").doubleValue();
                        double cambio = ((ultimo - primero) / primero) * 100;

                        lblPrecio.setText(String.format("%.2f", ultimo));
                        String signo = cambio >= 0 ? "+" : "";
                        lblCambio.setText(String.format("%s%.2f%%", signo, cambio));
                        String color = cambio >= 0 ? "#48bb78" : "#fc8181";
                        lblCambio.setStyle("-fx-font-size: 13px; -fx-text-fill: " + color + ";");

                        javafx.scene.Node linea = chart.lookup(".chart-series-line");
                        if (linea != null)
                            linea.setStyle("-fx-stroke: " + color + "; -fx-stroke-width: 2px;");
                    } else {
                        lblPrecio.setText("—");
                        lblCambio.setText("Sin datos");
                    }
                    limpiarFondoChart(chart);
                }
        );
    }

    // UTILIDADES

    private static void ajustarEjeY(LineChart<Number, Number> chart, double min, double max, double factor) {
        NumberAxis yAxis = (NumberAxis) chart.getYAxis();
        yAxis.setAutoRanging(false);
        double margen = (max - min) * factor;
        if (margen == 0) margen = max * 0.01;
        yAxis.setLowerBound(min - margen);
        yAxis.setUpperBound(max + margen);
        yAxis.setTickUnit((max - min + 2 * margen) / 5);
    }

    public static void limpiarFondoChart(LineChart<Number, Number> chart) {
        for (String sel : new String[]{".chart-plot-background", ".chart-content",
                ".chart-vertical-grid-lines", ".chart-horizontal-grid-lines",
                ".chart-alternative-row-fill"}) {
            javafx.scene.Node n = chart.lookup(sel);
            if (n != null) n.setStyle("-fx-background-color: transparent; -fx-stroke: transparent;");
        }
    }

    public static String emojiTipo(String tipo) {
        return switch (tipo) {
            case "CRIPTO" -> "₿"; case "ACCION" -> "📈";
            case "FONDO" -> "🏦"; default -> "💵";
        };
    }

    public static Label infoFila(String clave, String valor) {
        Label lbl = new Label(clave + ":  " + valor);
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #4a5568;");
        return lbl;
    }

    private static Label labelCampo(String texto) {
        Label lbl = new Label(texto);
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #4a5568; -fx-font-weight: bold;");
        return lbl;
    }

    private static TextField crearCampo(String placeholder) {
        TextField campo = new TextField();
        campo.setPromptText(placeholder);
        campo.setStyle(CAMPO);
        return campo;
    }

    // Los métodos públicos que usaban otros controladores se mantienen
    public static String estiloNormal() { return FILA_NORMAL; }
    public static String estiloHover() { return FILA_HOVER; }
    public static String estiloPeriodoInactivo() { return PERIODO_INACTIVO; }
    public static String estiloPeriodoActivo() { return PERIODO_ACTIVO; }

    public static String[] periodoToYFinance(String periodo) {
        return switch (periodo) {
            case "1D"  -> new String[]{"1d", "5m"};
            case "1S"  -> new String[]{"5d", "15m"};
            case "1M"  -> new String[]{"1mo", "1d"};
            case "3M"  -> new String[]{"3mo", "1d"};
            case "6M"  -> new String[]{"6mo", "1d"};
            case "1A"  -> new String[]{"1y", "1wk"};
            case "5A"  -> new String[]{"5y", "1mo"};
            case "Máx" -> new String[]{"max", "1mo"};
            default    -> new String[]{"1mo", "1d"};
        };
    }
}