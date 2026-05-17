package com.arkaly.desktop.controllers;

import static com.arkaly.desktop.utils.Estilos.*;

import com.arkaly.desktop.services.InversionService;
import com.arkaly.desktop.utils.ApiClient;
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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.*;

public class InversionesMercadoController {

    @FXML private VBox contenido;
    @FXML private TextField campoBusqueda;

    private final ObjectMapper mapper = new ObjectMapper();
    private List<JsonNode> activosCripto = new ArrayList<>(), activosAccion = new ArrayList<>(), activosFondo = new ArrayList<>();
    private Set<String> simbolosFavoritos = new HashSet<>(), simbolosConPosicion = new HashSet<>();
    private String periodoActual = "1M";

    @FXML public void initialize() {
        cargarDatos();
        contenido.setFillWidth(true);
        campoBusqueda.textProperty().addListener((obs, oldVal, newVal) -> {
            String txt = newVal.trim();
            if (txt.isEmpty()) renderizarSecciones();
            else if (txt.length() >= 2) buscarEnYFinance(txt);
        });
    }

    private void cargarDatos() {
        contenido.getChildren().clear();
        contenido.getChildren().add(new Label("⏳ Cargando mercado...") {{
            setStyle("-fx-font-size: 14px; -fx-text-fill: #718096;");
        }});

        // Listas locales que se llenan en el hilo de fondo
        final List<JsonNode>[] cripto  = new List[]{new ArrayList<>()};
        final List<JsonNode>[] accion  = new List[]{new ArrayList<>()};
        final List<JsonNode>[] fondo   = new List[]{new ArrayList<>()};
        final Set<String>[]    favs    = new Set[]{new HashSet<>()};
        final Set<String>[]    pos     = new Set[]{new HashSet<>()};

        AsyncTask.ejecutar(
                () -> {
                    JsonNode favsNode = mapper.readTree(ApiClient.getAuth("/activos/favoritos"));
                    for (JsonNode f : favsNode) favs[0].add(f.path("simbolo").asText());

                    JsonNode posNode = mapper.readTree(ApiClient.getAuth("/inversiones/posiciones"));
                    for (JsonNode p : posNode) pos[0].add(p.path("simbolo").asText());

                    for (JsonNode c : mapper.readTree(ApiClient.getAuth("/activos/mercado/CRIPTO"))) cripto[0].add(c);
                    for (JsonNode c : mapper.readTree(ApiClient.getAuth("/activos/mercado/ACCION"))) accion[0].add(c);
                    for (JsonNode c : mapper.readTree(ApiClient.getAuth("/activos/mercado/FONDO")))  fondo[0].add(c);
                },
                () -> {
                    // Sí estamos en el hilo renderizamos juntos
                    simbolosFavoritos   = favs[0];
                    simbolosConPosicion = pos[0];
                    activosCripto       = cripto[0];
                    activosAccion       = accion[0];
                    activosFondo        = fondo[0];
                    renderizarSecciones();
                }
        );
    }

    private HBox crearBarraFiltros() {
        HBox filtros = new HBox(8); filtros.setAlignment(Pos.CENTER_LEFT); filtros.setPadding(new Insets(0, 0, 10, 0));
        for (String p : new String[]{"1D", "1S", "1M", "3M", "6M", "1A", "5A", "Máx"}) {
            Button btn = new Button(p);
            btn.setStyle(p.equals(periodoActual) ? PERIODO_ACTIVO : PERIODO_INACTIVO);
            btn.setOnMouseClicked(e -> {
                periodoActual = p;
                String txt = campoBusqueda.getText().trim();
                if (txt.length() >= 2) buscarEnYFinance(txt); else renderizarSecciones();
            });
            filtros.getChildren().add(btn);
        }
        return filtros;
    }

    private void renderizarSecciones() {
        contenido.getChildren().clear();
        contenido.getChildren().add(crearBarraFiltros());
        contenido.getChildren().addAll(
                crearSeccion("₿  Criptomonedas", activosCripto, "CRIPTO"),
                crearSeccion("📈  Acciones", activosAccion, "ACCION"),
                crearSeccion("🏦  Fondos", activosFondo, "FONDO"));
    }

    private VBox crearSeccion(String titulo, List<JsonNode> activos, String tipo) {
        VBox seccion = new VBox(10); seccion.setMaxWidth(Double.MAX_VALUE); seccion.setPadding(new Insets(18));
        seccion.setStyle(CARD);

        Label tituloLbl = new Label(titulo); tituloLbl.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
        Label contador = new Label(activos.size() + " activos disponibles"); contador.setStyle("-fx-font-size: 11px; -fx-text-fill: #a0aec0;");
        HBox cab = new HBox(10, tituloLbl, contador); cab.setAlignment(Pos.CENTER_LEFT);

        HBox lista = new HBox(12); lista.setPadding(new Insets(6, 6, 10, 6)); lista.setAlignment(Pos.CENTER_LEFT);
        for (JsonNode a : activos) lista.getChildren().add(crearTarjetaActivo(a, tipo));

        ScrollPane scroll = new ScrollPane(lista);
        scroll.setFitToHeight(false); scroll.setFitToWidth(false);
        scroll.setPrefHeight(258); scroll.setMinHeight(258); scroll.setMaxHeight(258);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle(SCROLL_TRANSPARENTE);

        seccion.getChildren().addAll(cab, scroll);
        return seccion;
    }

    private VBox crearTarjetaActivo(JsonNode a, String tipo) {
        String simbolo = a.path("simbolo").asText();
        String nombre = a.path("nombre").asText();
        boolean esFav = simbolosFavoritos.contains(simbolo);

        Label estrella = new Label(esFav ? "⭐" : "☆");
        estrella.setStyle("-fx-font-size: 16px; -fx-cursor: hand; " + (esFav ? "-fx-text-fill: #f6ad55;" : "-fx-text-fill: #cbd5e0;"));
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
        tarjeta.setOnMouseClicked(e -> { if (e.getButton() == MouseButton.PRIMARY) InversionesUIHelper.abrirDetalle(a, tipo, simbolosConPosicion, contenido); });
        estrella.setOnMouseClicked(e -> { e.consume(); toggleFavorito(simbolo, nombre, tipo, estrella); });

        InversionesUIHelper.cargarHistoricoEnTarjeta(simbolo, tipo, periodoActual, serie, pctLabel, grafica);
        return tarjeta;
    }

    private void toggleFavorito(String simbolo, String nombre, String tipo, Label estrella) {
        boolean yaFav = simbolosFavoritos.contains(simbolo);
        AsyncTask.ejecutar(
                () -> {
                    if (yaFav) {
                        JsonNode favs = mapper.readTree(ApiClient.getAuth("/activos/favoritos"));
                        for (JsonNode f : favs) {
                            if (f.path("simbolo").asText().equals(simbolo)) {
                                ApiClient.deleteAuth("/activos/favoritos/" + f.path("id").asInt());
                                break;
                            }
                        }
                    } else {
                        String json = String.format("{\"simbolo\":\"%s\",\"nombre\":\"%s\",\"tipoActivo\":\"%s\"}", simbolo, nombre, tipo);
                        ApiClient.postAuth("/activos/favoritos", json);
                    }
                },
                () -> {
                    if (yaFav) {
                        simbolosFavoritos.remove(simbolo);
                        estrella.setText("☆"); estrella.setStyle("-fx-font-size: 16px; -fx-cursor: hand; -fx-text-fill: #cbd5e0;");
                    } else {
                        simbolosFavoritos.add(simbolo);
                        estrella.setText("⭐"); estrella.setStyle("-fx-font-size: 16px; -fx-cursor: hand; -fx-text-fill: #f6ad55;");
                    }
                }
        );
    }

    private void buscarEnYFinance(String query) {
        final List<JsonNode> resultados = new ArrayList<>();
        AsyncTask.ejecutar(
                () -> resultados.addAll(InversionService.buscar(query)),
                () -> {
                    contenido.getChildren().clear();
                    contenido.getChildren().add(crearBarraFiltros());
                    if (resultados.isEmpty()) {
                        contenido.getChildren().add(new Label("No se encontraron resultados para \"" + query + "\"") {{ setStyle("-fx-text-fill: #a0aec0; -fx-font-size: 14px;"); }});
                        return;
                    }
                    VBox seccion = new VBox(12); seccion.setPadding(new Insets(20)); seccion.setStyle(CARD);
                    Label titulo = new Label("🔍 Resultados para \"" + query + "\""); titulo.setFont(Font.font("System", FontWeight.BOLD, 16));
                    HBox lista = new HBox(14); lista.setPadding(new Insets(4));
                    for (JsonNode r : resultados) lista.getChildren().add(crearTarjetaActivo(r, r.path("tipoActivo").asText("ACCION")));
                    ScrollPane scroll = new ScrollPane(lista);
                    scroll.setFitToHeight(true); scroll.setPrefHeight(220);
                    scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                    scroll.setStyle(SCROLL_TRANSPARENTE);
                    seccion.getChildren().addAll(titulo, scroll);
                    contenido.getChildren().add(seccion);
                }
        );
    }

    //  NAVEGACIÓN

    @FXML public void mostrarGeneral()   { navegar("Inversiones_General.fxml"); }
    @FXML public void mostrarCartera()   { navegar("Inversiones_Cartera.fxml"); }
    @FXML public void mostrarHistorial() { navegar("Inversiones_Historial.fxml"); }
    @FXML public void mostrarAlertas()   { navegar("Inversiones_Alertas.fxml"); }

    private void navegar(String fxml) {
        try {
            javafx.scene.Node node = new javafx.fxml.FXMLLoader(getClass().getResource("/com/arkaly/desktop/" + fxml)).load();
            StackPane parent = (StackPane) contenido.getScene().lookup("#contenidoPanel");
            if (parent != null) parent.getChildren().setAll(node);
        } catch (Exception e) { e.printStackTrace(); }
    }
}