package com.arkaly.desktop.controllers;

import static com.arkaly.desktop.utils.Estilos.*;

import com.arkaly.desktop.services.InversionService;
import com.arkaly.desktop.utils.AsyncTask;
import com.arkaly.desktop.utils.InversionesUIHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

public class InversionesGeneralController {

    @FXML private ComboBox<String> selectorTipo;
    @FXML private Label labelTotal;
    @FXML private Label labelPorcentaje;
    @FXML private VBox contenedorFavoritos;
    @FXML private VBox contenedorAlertas;

    private HBox listaFavoritos;
    private final ObjectMapper mapper = new ObjectMapper();
    private JsonNode resumenData;
    private Set<String> simbolosConPosicion = new HashSet<>();

    @FXML public void initialize() {
        selectorTipo.getItems().addAll("Total", "Cripto", "Acciones", "Fondos");
        selectorTipo.getSelectionModel().selectFirst();
        selectorTipo.setOnAction(e -> actualizarTotal());

        listaFavoritos = new HBox(14);
        listaFavoritos.setPadding(new Insets(4, 4, 8, 4));
        listaFavoritos.setAlignment(Pos.CENTER_LEFT);

        ScrollPane scrollFav = new ScrollPane(listaFavoritos);
        scrollFav.setFitToHeight(false); scrollFav.setFitToWidth(false);
        scrollFav.setPrefHeight(230); scrollFav.setMinHeight(230);
        scrollFav.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollFav.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollFav.setStyle(SCROLL_TRANSPARENTE);
        if (contenedorFavoritos != null) contenedorFavoritos.getChildren().add(scrollFav);

        cargarDatos();
    }

    private void cargarDatos() {
        labelTotal.setText("Cargando..."); labelPorcentaje.setText("");

        final JsonNode[] resumen = {null};
        final JsonNode[] favoritos = {null};
        final Set<String> posSet = new HashSet<>();

        AsyncTask.ejecutar(
                () -> {
                    resumen[0] = InversionService.getResumen();
                    favoritos[0] = mapper.readTree(com.arkaly.desktop.utils.ApiClient.getAuth("/activos/favoritos"));
                    posSet.addAll(InversionService.getSimbolosConPosicion());
                },
                () -> {
                    simbolosConPosicion = posSet;
                    resumenData = resumen[0];
                    actualizarTotal();
                    cargarFavoritos(favoritos[0]);
                    cargarAlertasDetalle();
                }
        );
    }

    private void actualizarTotal() {
        if (resumenData == null) return;
        String sel = selectorTipo.getValue();

        if ("Total".equals(sel)) {
            labelTotal.setText(resumenData.path("valorActual").asText("0.00") + " €");
            mostrarPorcentaje(new BigDecimal(resumenData.path("porcentajeVariacion").asText("0.00")));
        } else {
            String clave = switch (sel) { case "Cripto" -> "CRIPTO"; case "Acciones" -> "ACCION"; case "Fondos" -> "FONDO"; default -> ""; };
            JsonNode valorPorTipo = resumenData.path("valorActualPorTipo");
            JsonNode pctPorTipo = resumenData.path("porcentajePorTipo");
            labelTotal.setText(valorPorTipo.has(clave) ? valorPorTipo.get(clave).asText() + " €" : "0.00 €");
            if (pctPorTipo.has(clave)) mostrarPorcentaje(new BigDecimal(pctPorTipo.get(clave).asText()));
            else { labelPorcentaje.setText("Sin datos"); labelPorcentaje.setStyle("-fx-font-size: 13px; -fx-text-fill: #a0aec0;"); }
        }
    }

    private void mostrarPorcentaje(BigDecimal pct) {
        boolean sube = pct.compareTo(BigDecimal.ZERO) >= 0;
        String color = sube ? "#48bb78" : "#fc8181";
        labelPorcentaje.setText((sube ? "▲ +" : "▼ ") + pct.toPlainString() + "%");
        labelPorcentaje.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
    }

    //FAVORITOS

    private void cargarFavoritos(JsonNode favoritos) {
        listaFavoritos.getChildren().clear();
        if (favoritos == null || favoritos.isEmpty()) {
            listaFavoritos.getChildren().add(new Label("No sigues ningún activo todavía") {{ setStyle("-fx-text-fill: #a0aec0; -fx-font-size: 13px;"); }});
            return;
        }
        for (JsonNode f : favoritos) listaFavoritos.getChildren().add(crearTarjetaFavorito(f));
    }

    private VBox crearTarjetaFavorito(JsonNode f) {
        String simbolo = f.path("simbolo").asText();
        String nombre = f.path("nombre").asText();
        String tipo = f.path("tipoActivo").asText();
        int idFav = f.path("id").asInt();

        Label estrella = new Label("⭐");
        estrella.setStyle("-fx-font-size: 16px; -fx-cursor: hand; -fx-text-fill: #f6ad55;");
        estrella.setOnMouseClicked(e -> { e.consume(); eliminarFavorito(idFav); });
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        HBox topRow = new HBox(sp, estrella);

        StackPane logo = new StackPane();
        logo.setPrefSize(40, 40); logo.setMinSize(40, 40); logo.setMaxSize(40, 40);
        logo.setStyle("-fx-background-color: #edf2f7; -fx-background-radius: 20px;");
        logo.getChildren().add(new Label(InversionesUIHelper.emojiTipo(tipo)) {{ setStyle("-fx-font-size: 18px;"); }});

        Label lblNom = new Label(nombre); lblNom.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #2d3748;"); lblNom.setMaxWidth(130);
        Label lblSim = new Label(simbolo + " · " + tipo); lblSim.setStyle("-fx-font-size: 10px; -fx-text-fill: #a0aec0;");

        LineChart<Number, Number> grafica = InversionesUIHelper.crearMiniGrafica();
        XYChart.Series<Number, Number> serie = new XYChart.Series<>(); grafica.getData().add(serie);
        Label pctLabel = new Label("cargando..."); pctLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #a0aec0;");

        VBox tarjeta = new VBox(5);
        tarjeta.setPrefWidth(155); tarjeta.setMinWidth(155); tarjeta.setMaxWidth(155);
        tarjeta.setPrefHeight(215); tarjeta.setMinHeight(215);
        tarjeta.setPadding(new Insets(12)); tarjeta.setAlignment(Pos.TOP_LEFT);
        tarjeta.setStyle(FILA_NORMAL);
        tarjeta.getChildren().addAll(topRow, logo, lblNom, lblSim, grafica, pctLabel);
        tarjeta.setOnMouseEntered(e -> tarjeta.setStyle(FILA_HOVER));
        tarjeta.setOnMouseExited(e -> tarjeta.setStyle(FILA_NORMAL));
        tarjeta.setOnMouseClicked(e -> { if (e.getButton() == MouseButton.PRIMARY) InversionesUIHelper.abrirDetalle(f, tipo, simbolosConPosicion, labelTotal); });

        InversionesUIHelper.cargarHistoricoEnTarjeta(simbolo, tipo, "1M", serie, pctLabel, grafica);
        return tarjeta;
    }

    private void eliminarFavorito(int id) {
        AsyncTask.ejecutar(() -> com.arkaly.desktop.utils.ApiClient.deleteAuth("/activos/favoritos/" + id), this::cargarDatos);
    }

    // ALERTAS

    private void cargarAlertasDetalle() {
        final JsonNode[] alertas = {null};
        AsyncTask.ejecutar(
                () -> alertas[0] = mapper.readTree(com.arkaly.desktop.utils.ApiClient.getAuth("/alertas/con-precio")),
                () -> {
                    while (contenedorAlertas.getChildren().size() > 1) contenedorAlertas.getChildren().remove(1);
                    if (alertas[0] == null || !alertas[0].isArray() || alertas[0].isEmpty()) {
                        contenedorAlertas.getChildren().add(new Label("No tienes alertas configuradas") {{ setStyle("-fx-text-fill: #a0aec0; -fx-font-size: 13px;"); }});
                        return;
                    }
                    HBox lista = new HBox(14); lista.setPadding(new Insets(4, 4, 8, 4)); lista.setAlignment(Pos.TOP_LEFT);
                    ScrollPane scroll = new ScrollPane(lista);
                    scroll.setFitToHeight(false); scroll.setFitToWidth(true);
                    scroll.setPrefHeight(245); scroll.setMinHeight(245);
                    scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                    scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                    scroll.setStyle(SCROLL_TRANSPARENTE);
                    for (JsonNode a : alertas[0]) lista.getChildren().add(crearTarjetaAlerta(a));
                    contenedorAlertas.getChildren().add(scroll);
                }
        );
    }

    private VBox crearTarjetaAlerta(JsonNode a) {
        String simbolo = a.path("simbolo").asText();
        String tipo = a.path("tipoActivo").asText();
        String condicion = a.path("condicion").asText();
        double precioObj = a.path("precioObjetivo").asDouble();
        double precioAct = a.path("precioActual").asDouble();
        boolean disparada = a.path("disparada").asBoolean();
        boolean esMayor = "MAYOR_QUE".equals(condicion);

        double progreso;
        if (esMayor) {
            progreso = precioObj > 0 ? (precioAct / precioObj) * 100 : 0;
        } else {
            if (precioAct <= precioObj) {
                progreso = 100;
            } else {
                progreso = (precioObj / precioAct) * 100;
            }
        }
        progreso = Math.min(Math.max(progreso, 0), 100);

        Label lblEstado = new Label(disparada ? "✅ Disparada" : "⏳ Pendiente");
        lblEstado.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: " + (disparada ? "#48bb78" : "#a0aec0") + ";");
        Region spTop = new Region(); HBox.setHgrow(spTop, Priority.ALWAYS);
        HBox filaTop = new HBox(spTop, lblEstado); filaTop.setAlignment(Pos.TOP_RIGHT);

        Label lblEmoji = new Label(InversionesUIHelper.emojiTipo(tipo)); lblEmoji.setStyle("-fx-font-size: 18px;");
        Label lblSim = new Label(simbolo); lblSim.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
        VBox infoSim = new VBox(1, lblEmoji, lblSim); infoSim.setAlignment(Pos.TOP_LEFT);

        Label lblTipo = new Label(tipo); lblTipo.setStyle("-fx-font-size: 10px; -fx-text-fill: #a0aec0;");
        Label lblCond = new Label(esMayor ? "▲ Mayor que" : "▼ Menor que");
        lblCond.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: " + (esMayor ? "#48bb78" : "#fc8181") + ";");

        Label lblAct = new Label(String.format("%.2f €", precioAct));
        lblAct.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
        Label lblObj = new Label(String.format("Obj: %.2f €", precioObj));
        lblObj.setStyle("-fx-font-size: 10px; -fx-text-fill: #718096;");

        String barColor = disparada ? "#48bb78" : "#4a90e2";
        ProgressBar barra = new ProgressBar(progreso / 100);
        barra.setPrefWidth(Double.MAX_VALUE); barra.setMaxWidth(Double.MAX_VALUE); barra.setPrefHeight(6);
        barra.setStyle("-fx-accent: " + barColor + ";");

        double pctFalta = 100 - progreso;
        Label lblPct = new Label(
                disparada
                        ? "🎯 Objetivo alcanzado"
                        : String.format("%.1f%% para el objetivo", pctFalta)
        );
        lblPct.setStyle("-fx-font-size: 10px; -fx-text-fill: " + (disparada ? "#48bb78" : "#718096") + ";");
        lblPct.setWrapText(true);

        String borderColor = disparada ? "#48bb78" : "#e2e8f0";
        String estiloBase = "-fx-background-color: #f7fafc; -fx-background-radius: 10px; -fx-border-color: " + borderColor + "; -fx-border-radius: 10px; -fx-cursor: hand;";
        String estiloHover = "-fx-background-color: #ebf8ff; -fx-background-radius: 10px; -fx-border-color: " + (disparada ? "#48bb78" : "#bee3f8") + "; -fx-border-radius: 10px; -fx-cursor: hand;";

        VBox tarjeta = new VBox(3, filaTop, infoSim, lblTipo, lblCond, lblAct, lblObj, barra, lblPct);
        tarjeta.setPrefWidth(170); tarjeta.setMinWidth(170); tarjeta.setMaxWidth(170);
        tarjeta.setPrefHeight(225); tarjeta.setMinHeight(225);
        tarjeta.setPadding(new Insets(12)); tarjeta.setAlignment(Pos.TOP_LEFT);
        tarjeta.setStyle(estiloBase);
        tarjeta.setOnMouseEntered(e -> tarjeta.setStyle(estiloHover));
        tarjeta.setOnMouseExited(e -> tarjeta.setStyle(estiloBase));
        tarjeta.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                com.fasterxml.jackson.databind.node.ObjectNode nodo = mapper.createObjectNode();
                nodo.put("simbolo", simbolo); nodo.put("tipoActivo", tipo);
                nodo.put("nombre", a.path("nombre").asText(simbolo));
                InversionesUIHelper.abrirDetalle(nodo, tipo, simbolosConPosicion, labelTotal);
            }
        });
        return tarjeta;
    }

    // NAVEGACIÓN

    @FXML public void mostrarOrdenes()   { navegar("Inversiones_Ordenes.fxml"); }
    @FXML public void mostrarCartera()   { navegar("Inversiones_Cartera.fxml"); }
    @FXML public void mostrarHistorial() { navegar("Inversiones_Historial.fxml"); }
    @FXML public void mostrarAlertas()   { navegar("Inversiones_Alertas.fxml"); }

    private void navegar(String fxml) {
        try {
            javafx.scene.Node node = new javafx.fxml.FXMLLoader(getClass().getResource("/com/arkaly/desktop/" + fxml)).load();
            StackPane parent = (StackPane) labelTotal.getScene().lookup("#contenidoPanel");
            if (parent != null) parent.getChildren().setAll(node);
        } catch (Exception e) { e.printStackTrace(); }
    }
}