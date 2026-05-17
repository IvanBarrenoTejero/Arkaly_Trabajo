package com.arkaly.desktop.controllers;

import com.arkaly.desktop.services.InformeService;
import com.arkaly.desktop.utils.AsyncTask;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

public class GraficosController {

    @FXML private PieChart pieGastos;
    @FXML private BarChart<String, Number> barIngresosGastos;
    @FXML private TableView<Map<String, Object>> tablaGastos;
    @FXML private TableColumn<Map<String, Object>, String> colCatNombre, colCatTotal, colCatPct;
    @FXML private Label lblTotalIngresos, lblTotalGastos, lblBalance;
    @FXML private Label lblRegla, lblMediaIngresos, lblMediaGastos, lblAhorroSugerido, lblAhorroReal, lblMensajeProyeccion;
    @FXML private DatePicker fechaDesde, fechaHasta;
    @FXML private Label lblValorCartera, lblTotalInvertido, lblPnlInversiones, lblPnlPorcentaje, lblPatrimonioTotal, lblNumPosiciones;

    private final String[] COLORES = {"#4299e1","#48bb78","#fc8181","#f6ad55","#9f7aea","#76e4f7","#fbb6ce","#b7eb8f","#ffd666","#d3adf7"};

    @FXML public void initialize() {
        configurarTabla();
        fechaDesde.setValue(LocalDate.now().minusMonths(6));
        fechaHasta.setValue(LocalDate.now());
        cargar(fechaDesde.getValue().toString(), fechaHasta.getValue().toString());
    }

    @FXML private void aplicarFiltro() {
        cargar(fechaDesde.getValue().toString(), fechaHasta.getValue().toString());
    }

    public void cargar(String desde, String hasta) {
        cargarGastosPorCategoria(desde, hasta);
        cargarIngresosVsGastos(desde, hasta);
        cargarProyeccion();
    }

    private void cargarGastosPorCategoria(String desde, String hasta) {
        final List<PieChart.Data> pieData = new ArrayList<>();
        final List<Map<String, Object>> tablaData = new ArrayList<>();

        AsyncTask.ejecutar(
                () -> {
                    JsonNode arr = InformeService.getGastosPorCategoria(desde, hasta);
                    for (JsonNode n : arr) {
                        String nombre = n.get("nombreCategoria").asText();
                        double total = n.get("total").asDouble();
                        double pct = n.get("porcentaje").asDouble();
                        pieData.add(new PieChart.Data(nombre + " (" + String.format("%.1f", pct) + "%)", total));
                        Map<String, Object> row = new LinkedHashMap<>();
                        row.put("nombre", nombre); row.put("total", String.format("%.2f €", total));
                        row.put("pct", String.format("%.1f%%", pct));
                        row.put("color", n.has("color") && !n.get("color").isNull() ? n.get("color").asText() : "#4299e1");
                        tablaData.add(row);
                    }
                },
                () -> {
                    pieGastos.setData(FXCollections.observableArrayList(pieData));
                    int i = 0;
                    for (PieChart.Data d : pieGastos.getData()) {
                        String color = i < tablaData.size() ? (String) tablaData.get(i).get("color") : COLORES[i % COLORES.length];
                        d.getNode().setStyle("-fx-pie-color: " + color + ";");
                        i++;
                    }
                    tablaGastos.setItems(FXCollections.observableArrayList(tablaData));
                }
        );
    }

    private void cargarIngresosVsGastos(String desde, String hasta) {
        final XYChart.Series<String, Number> sIng = new XYChart.Series<>(), sGas = new XYChart.Series<>(), sInv = new XYChart.Series<>();
        sIng.setName("Ingresos"); sGas.setName("Gastos"); sInv.setName("Inversiones");
        final BigDecimal[] sums = {BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO};

        AsyncTask.ejecutar(
                () -> {
                    JsonNode arr = InformeService.getIngresosVsGastos(desde, hasta);
                    for (JsonNode n : arr) {
                        String etq = n.get("etiquetaMes").asText();
                        double ing = n.get("totalIngresos").asDouble(), gas = n.get("totalGastos").asDouble();
                        double inv = n.has("gananciaInversiones") ? n.get("gananciaInversiones").asDouble() : 0;
                        sIng.getData().add(new XYChart.Data<>(etq, ing));
                        sGas.getData().add(new XYChart.Data<>(etq, gas));
                        sInv.getData().add(new XYChart.Data<>(etq, inv));
                        sums[0] = sums[0].add(BigDecimal.valueOf(ing));
                        sums[1] = sums[1].add(BigDecimal.valueOf(gas));
                        sums[2] = sums[2].add(BigDecimal.valueOf(inv));
                    }
                },
                () -> {
                    barIngresosGastos.getData().clear();
                    barIngresosGastos.getData().addAll(sIng, sGas, sInv);
                    colorearBarras();
                    lblTotalIngresos.setText(String.format("%.2f €", sums[0].doubleValue()));
                    lblTotalGastos.setText(String.format("%.2f €", sums[1].doubleValue()));
                    BigDecimal balance = sums[0].subtract(sums[1]).add(sums[2]);
                    lblBalance.setText(String.format("%.2f €", balance.doubleValue()));
                    lblBalance.setStyle("-fx-text-fill: " + (balance.signum() >= 0 ? "#48bb78" : "#fc8181") + ";");
                }
        );
    }

    private void colorearBarras() {
        barIngresosGastos.lookupAll(".default-color0.chart-bar").forEach(n -> n.setStyle("-fx-bar-fill: #48bb78;"));
        barIngresosGastos.lookupAll(".default-color1.chart-bar").forEach(n -> n.setStyle("-fx-bar-fill: #fc8181;"));
        barIngresosGastos.lookupAll(".default-color2.chart-bar").forEach(n -> n.setStyle("-fx-bar-fill: #9f7aea;"));
    }

    private void cargarProyeccion() {
        final JsonNode[] data = {null};
        AsyncTask.ejecutar(
                () -> data[0] = InformeService.getProyeccion(),
                () -> {
                    JsonNode n = data[0];
                    if (n == null) return;
                    lblRegla.setText(n.get("reglaFinanciera").asText());
                    lblMediaIngresos.setText(String.format("%.2f €", n.get("mediaIngresosMensual").asDouble()));
                    lblMediaGastos.setText(String.format("%.2f €", n.get("mediaGastosMensual").asDouble()));
                    lblAhorroSugerido.setText(String.format("%.2f €", n.get("ahorroSugerido").asDouble()));
                    lblAhorroReal.setText(String.format("%.2f €", n.get("ahorroReal").asDouble()));
                    lblMensajeProyeccion.setText("💡 " + n.get("mensaje").asText());

                    setLabelSiExiste(lblValorCartera, n, "valorCartera", "%.2f €");
                    setLabelSiExiste(lblTotalInvertido, n, "totalInvertido", "%.2f €");
                    if (lblPnlInversiones != null && n.has("pnlInversiones")) {
                        double pnl = n.get("pnlInversiones").asDouble();
                        lblPnlInversiones.setText(String.format("%+.2f €", pnl));
                        lblPnlInversiones.setStyle("-fx-text-fill: " + (pnl >= 0 ? "#48bb78" : "#fc8181") + ";");
                    }
                    if (lblPnlPorcentaje != null && n.has("pnlPorcentaje")) {
                        double pct = n.get("pnlPorcentaje").asDouble();
                        lblPnlPorcentaje.setText(String.format("%+.2f%%", pct));
                        lblPnlPorcentaje.setStyle("-fx-text-fill: " + (pct >= 0 ? "#48bb78" : "#fc8181") + ";");
                    }
                    setLabelSiExiste(lblPatrimonioTotal, n, "patrimonioTotal", "%.2f €");
                    if (lblNumPosiciones != null && n.has("numeroPosiciones"))
                        lblNumPosiciones.setText(n.get("numeroPosiciones").asInt() + " posiciones activas");
                }
        );
    }

    private void setLabelSiExiste(Label lbl, JsonNode n, String campo, String formato) {
        if (lbl != null && n.has(campo)) lbl.setText(String.format(formato, n.get(campo).asDouble()));
    }

    @FXML public void abrirCambioRegla() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Regla Financiera");
        VBox contenido = new VBox(16); contenido.setStyle("-fx-padding: 24;"); contenido.setPrefWidth(380);
        Label lblInfo = new Label("Selecciona la regla de ahorro que quieres aplicar:");
        lblInfo.setWrapText(true); lblInfo.setStyle("-fx-text-fill: #4a5568; -fx-font-size: 13px;");

        ToggleGroup grupo = new ToggleGroup();
        RadioButton rb1 = crearRadio("50/30/20", "50% necesidades · 30% ocio · 20% ahorro", "CINCUENTA_TREINTA_VEINTE", grupo);
        RadioButton rb2 = crearRadio("Porcentaje Ahorro", "70% gastos · 20% ahorro · 10% inversión", "PORCENTAJE_AHORRO", grupo);
        RadioButton rb3 = crearRadio("Kakeibo", "60% gastos · 20% ocio · 20% ahorro", "KAKEIBO", grupo);

        Map<String, String> map = Map.of("50/30/20", "CINCUENTA_TREINTA_VEINTE", "70/20/10", "PORCENTAJE_AHORRO", "60/20/20", "KAKEIBO");
        String enumActual = map.getOrDefault(lblRegla.getText(), "CINCUENTA_TREINTA_VEINTE");
        grupo.getToggles().forEach(t -> { if (t.getUserData().equals(enumActual)) t.setSelected(true); });
        if (grupo.getSelectedToggle() == null) rb1.setSelected(true);

        contenido.getChildren().addAll(lblInfo, rb1, rb2, rb3);
        dialog.getDialogPane().setContent(contenido);
        dialog.getDialogPane().setMinWidth(420);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK && grupo.getSelectedToggle() != null)
                AsyncTask.ejecutar(() -> InformeService.actualizarRegla((String) grupo.getSelectedToggle().getUserData()), this::cargarProyeccion);
        });
    }

    private RadioButton crearRadio(String titulo, String desc, String userData, ToggleGroup grupo) {
        VBox box = new VBox(2);
        box.getChildren().addAll(
                new Label(titulo) {{ setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2d3748;"); }},
                new Label(desc) {{ setStyle("-fx-font-size: 11px; -fx-text-fill: #718096;"); }});
        RadioButton rb = new RadioButton(); rb.setToggleGroup(grupo); rb.setUserData(userData);
        rb.setGraphic(box); rb.setStyle("-fx-padding: 8 0 8 0;"); return rb;
    }

    private void configurarTabla() {
        colCatNombre.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty((String) d.getValue().get("nombre")));
        colCatTotal.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty((String) d.getValue().get("total")));
        colCatPct.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty((String) d.getValue().get("pct")));
    }
}