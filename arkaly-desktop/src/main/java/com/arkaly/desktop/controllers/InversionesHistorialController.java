package com.arkaly.desktop.controllers;

import static com.arkaly.desktop.utils.Estilos.*;

import com.arkaly.desktop.services.InversionService;
import com.arkaly.desktop.utils.AsyncTask;
import com.arkaly.desktop.utils.InversionesUIHelper;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class InversionesHistorialController {

    @FXML private VBox listaOperaciones;
    @FXML private ComboBox<String> selectorTipoHistorial;
    @FXML private Label lblResumenTotal;

    private String tipoSeleccionado = "TODOS";

    @FXML public void initialize() {
        selectorTipoHistorial.getItems().addAll("Todos", "Cripto", "Acciones", "Fondos");
        selectorTipoHistorial.getSelectionModel().selectFirst();
        selectorTipoHistorial.setOnAction(e -> {
            tipoSeleccionado = mapTipo(selectorTipoHistorial.getValue());
            cargarOperaciones();
        });
        cargarOperaciones();
    }

    private String mapTipo(String sel) {
        return switch (sel) { case "Cripto" -> "CRIPTO"; case "Acciones" -> "ACCION"; case "Fondos" -> "FONDO"; default -> "TODOS"; };
    }

    private void cargarOperaciones() {
        listaOperaciones.getChildren().clear();
        Label cargando = new Label("⏳ Cargando historial..."); cargando.setStyle("-fx-font-size: 14px; -fx-text-fill: #718096;");
        listaOperaciones.getChildren().add(cargando);

        final JsonNode[] ops = {null};
        AsyncTask.ejecutar(
                () -> ops[0] = InversionService.getOperaciones(tipoSeleccionado),
                () -> {
                    listaOperaciones.getChildren().clear();
                    if (ops[0] == null || !ops[0].isArray() || ops[0].isEmpty()) {
                        listaOperaciones.getChildren().add(new Label("No tienes operaciones registradas") {{ setStyle("-fx-text-fill: #a0aec0; -fx-font-size: 14px;"); }});
                        if (lblResumenTotal != null) lblResumenTotal.setText("");
                        return;
                    }

                    double totalCompras = 0, totalVentas = 0; int nC = 0, nV = 0;
                    for (JsonNode op : ops[0]) {
                        double pt = op.path("precioTotal").asDouble();
                        if ("COMPRA".equals(op.path("tipoOperacion").asText())) { totalCompras += pt; nC++; }
                        else { totalVentas += pt; nV++; }
                    }
                    if (lblResumenTotal != null)
                        lblResumenTotal.setText(String.format("%d ops  ·  %d compras (%.2f €)  ·  %d ventas (%.2f €)  ·  Neto: %.2f €",
                                ops[0].size(), nC, totalCompras, nV, totalVentas, totalCompras - totalVentas));

                    listaOperaciones.getChildren().add(crearCabecera());
                    for (JsonNode op : ops[0]) listaOperaciones.getChildren().add(crearFilaOperacion(op));
                }
        );
    }

    private HBox crearCabecera() {
        HBox cab = new HBox();
        cab.setAlignment(Pos.CENTER_LEFT);
        cab.setPadding(new Insets(10, 16, 10, 16));
        cab.setStyle("-fx-background-color: #edf2f7; -fx-background-radius: 10px;");
        cab.setMaxWidth(Double.MAX_VALUE);

        for (String[] col : new String[][]{
                {"Fecha", "1"},
                {"Símbolo", "1"},
                {"Tipo", "1"},
                {"Operación", "1"},
                {"Cantidad", "1"},
                {"Precio ud.", "1"},
                {"Total", "1"},
                {"Notas", "2"}
        }) {

            Label l = new Label(col[0]);
            l.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #4a5568;");

            HBox.setHgrow(l, Priority.ALWAYS);
            l.setMaxWidth(Double.MAX_VALUE);

            double peso = Double.parseDouble(col[1]);

            l.setMinWidth(50 * peso);
            l.setPrefWidth(80 * peso);

            cab.getChildren().add(l);
        }

        return cab;
    }

    private HBox crearFilaOperacion(JsonNode op) {
        boolean esCompra = "COMPRA".equals(op.path("tipoOperacion").asText());
        String tipoActivo = op.path("tipoActivo").asText();

        HBox fila = new HBox(); fila.setAlignment(Pos.CENTER_LEFT);
        fila.setPadding(new Insets(12, 16, 12, 16)); fila.setMaxWidth(Double.MAX_VALUE);
        fila.setStyle("-fx-background-color: white; -fx-background-radius: 10px; -fx-border-color: #e2e8f0; -fx-border-radius: 10px;");
        fila.setOnMouseEntered(e -> fila.setStyle("-fx-background-color: #f7fafc; -fx-background-radius: 10px; -fx-border-color: #bee3f8; -fx-border-radius: 10px;"));
        fila.setOnMouseExited(e -> fila.setStyle("-fx-background-color: white; -fx-background-radius: 10px; -fx-border-color: #e2e8f0; -fx-border-radius: 10px;"));

        Label lblFecha = celda(op.path("fechaOperacion").asText(""), 1);
        lblFecha.setStyle(lblFecha.getStyle() + " -fx-text-fill: #718096;");
        Label lblSim = celda(InversionesUIHelper.emojiTipo(tipoActivo) + " " + op.path("simbolo").asText(""), 1);
        lblSim.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2d3748;"); HBox.setHgrow(lblSim, Priority.ALWAYS); lblSim.setMaxWidth(Double.MAX_VALUE);
        Label lblTipo = celda(tipoActivo, 1);
        Label lblOp = new Label(esCompra ? "COMPRA" : "VENTA");
        lblOp.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + (esCompra ? "#48bb78" : "#fc8181") + ";");
        HBox.setHgrow(lblOp, Priority.ALWAYS); lblOp.setMaxWidth(Double.MAX_VALUE); lblOp.setMinWidth(50); lblOp.setPrefWidth(80);
        Label lblCant = celda(String.format("%.4f", op.path("cantidad").asDouble()), 1);
        Label lblPrec = celda(String.format("%.2f €", op.path("precioUnitario").asDouble()), 1);
        Label lblTot = celda(String.format("%.2f €", op.path("precioTotal").asDouble()), 1);
        lblTot.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2d3748;"); HBox.setHgrow(lblTot, Priority.ALWAYS); lblTot.setMaxWidth(Double.MAX_VALUE);
        String notas = op.path("notas").asText("");
        Label lblNotas = celda(notas.isEmpty() ? "—" : notas, 2);
        lblNotas.setStyle(lblNotas.getStyle() + " -fx-text-fill: #a0aec0;");

        fila.getChildren().addAll(lblFecha, lblSim, lblTipo, lblOp, lblCant, lblPrec, lblTot, lblNotas);
        return fila;
    }

    private Label celda(String texto, double peso) {
        Label l = new Label(texto); l.setStyle("-fx-font-size: 12px; -fx-text-fill: #4a5568;");
        HBox.setHgrow(l, Priority.ALWAYS); l.setMaxWidth(Double.MAX_VALUE);
        l.setMinWidth(50 * peso); l.setPrefWidth(80 * peso); return l;
    }

    // NAVEGACIÓN

    @FXML public void mostrarGeneral()  { navegar("Inversiones_General.fxml"); }
    @FXML public void mostrarCartera()  { navegar("Inversiones_Cartera.fxml"); }
    @FXML public void mostrarOrdenes()  { navegar("Inversiones_Ordenes.fxml"); }
    @FXML public void mostrarAlertas()  { navegar("Inversiones_Alertas.fxml"); }

    private void navegar(String fxml) {
        try {
            javafx.scene.Node node = new javafx.fxml.FXMLLoader(getClass().getResource("/com/arkaly/desktop/" + fxml)).load();
            StackPane parent = (StackPane) listaOperaciones.getScene().lookup("#contenidoPanel");
            if (parent != null) parent.getChildren().setAll(node);
        } catch (Exception e) { e.printStackTrace(); }
    }
}