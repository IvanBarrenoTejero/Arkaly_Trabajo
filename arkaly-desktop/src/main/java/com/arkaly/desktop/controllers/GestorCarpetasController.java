package com.arkaly.desktop.controllers;

import static com.arkaly.desktop.utils.Estilos.*;

import com.arkaly.desktop.services.CarpetaService;
import com.arkaly.desktop.services.DocumentoService;
import com.arkaly.desktop.services.EtiquetaService;
import com.arkaly.desktop.utils.AsyncTask;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GestorCarpetasController {

    @FXML private FlowPane flowPane;
    @FXML private ListView<String> listaView;
    @FXML private ScrollPane scrollCuadricula;
    @FXML private Label rutaLabel;
    @FXML private ToggleButton toggleVista;
    @FXML private Button btnAtras;
    @FXML private Button btnSubirDocumento;
    @FXML private TextField campoBusqueda;
    @FXML private ComboBox<String> filtroTipo;
    @FXML private ComboBox<String> filtroEtiqueta;
    @FXML private ToggleButton filtroTodos;
    @FXML private ToggleButton filtroCarpetas;
    @FXML private ToggleButton filtroDocumentos;
    @FXML private ToggleButton toggleBusquedaGlobal;

    private boolean vistaLista = false;
    private boolean busquedaGlobal = false;

    private final List<Integer> historialIds = new ArrayList<>();
    private final List<String> historialNombres = new ArrayList<>();
    private Integer carpetaActualId = null;

    private List<JsonNode> carpetasActuales = new ArrayList<>();
    private List<JsonNode> etiquetasDisponibles = new ArrayList<>();
    private List<JsonNode> documentosActuales = new ArrayList<>();

    @FXML
    public void initialize() {
        btnAtras.setDisable(true);
        configurarFiltros();
        cargarEtiquetas();
        cargarCarpetas(null, "Mis Carpetas");
    }

    private void configurarFiltros() {
        filtroTipo.setItems(FXCollections.observableArrayList(
                "Todos los tipos", "PDF", "Imagen", "Word", "Excel"));
        filtroTipo.getSelectionModel().selectFirst();

        ToggleGroup grupo = new ToggleGroup();
        filtroTodos.setToggleGroup(grupo);
        filtroCarpetas.setToggleGroup(grupo);
        filtroDocumentos.setToggleGroup(grupo);
        filtroTodos.setSelected(true);

        grupo.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) { filtroTodos.setSelected(true); return; }
            filtroTodos.setStyle(filtroTodos.isSelected() ? TOGGLE_ACTIVO : TOGGLE_INACTIVO);
            filtroCarpetas.setStyle(filtroCarpetas.isSelected() ? TOGGLE_ACTIVO : TOGGLE_INACTIVO);
            filtroDocumentos.setStyle(filtroDocumentos.isSelected() ? TOGGLE_ACTIVO : TOGGLE_INACTIVO);
        });
    }

    // CARGA DE DATOS

    private void cargarEtiquetas() {
        AsyncTask.ejecutar(
                () -> etiquetasDisponibles = EtiquetaService.getAll(),
                () -> {
                    List<String> nombres = new ArrayList<>();
                    nombres.add("Todas las etiquetas");
                    for (JsonNode e : etiquetasDisponibles) nombres.add(e.get("nombre").asText());
                    filtroEtiqueta.setItems(FXCollections.observableArrayList(nombres));
                    filtroEtiqueta.getSelectionModel().selectFirst();
                }
        );
    }

    private void cargarCarpetas(Integer idPadre, String titulo) {
        final List<JsonNode> carpetas = new ArrayList<>();
        final List<JsonNode> documentos = new ArrayList<>();

        AsyncTask.ejecutar(
                () -> {
                    if (idPadre == null) carpetas.addAll(CarpetaService.getRaiz());
                    else {
                        carpetas.addAll(CarpetaService.getSubcarpetas(idPadre));
                        documentos.addAll(DocumentoService.getPorCarpeta(idPadre));
                    }
                },
                () -> {
                    carpetasActuales = carpetas;
                    documentosActuales = documentos;
                    rutaLabel.setText(titulo);
                    btnSubirDocumento.setVisible(idPadre != null);
                    renderizar();
                }
        );
    }

    // RENDERIZADO

    private void renderizar() {
        String busqueda = campoBusqueda.getText().toLowerCase().trim();
        String tipoSel = filtroTipo.getValue();
        String etiquetaSel = filtroEtiqueta.getValue();
        boolean hayFiltroTipo = tipoSel != null && !tipoSel.equals("Todos los tipos");
        boolean mostrarCarpetas = (filtroTodos.isSelected() || filtroCarpetas.isSelected()) && !hayFiltroTipo;
        boolean mostrarDocs = filtroTodos.isSelected() || filtroDocumentos.isSelected();

        if (vistaLista) {
            scrollCuadricula.setVisible(false);
            listaView.setVisible(true);
            listaView.getItems().clear();
            if (mostrarCarpetas) {
                for (JsonNode c : carpetasActuales) {
                    if (!pasaFiltro(c.get("nombre").asText(), busqueda, c, etiquetaSel)) continue;
                    String etq = tieneEtiqueta(c) ? " 🏷️ " + c.get("etiqueta").get("nombre").asText() : "";
                    listaView.getItems().add("📁 " + c.get("nombre").asText() + etq);
                }
            }
            if (mostrarDocs) {
                for (JsonNode d : documentosActuales) {
                    if (!pasaFiltro(d.get("nombreOriginal").asText(), busqueda, d, etiquetaSel)) continue;
                    if (!pasaFiltroTipo(d.get("tipoMime").asText(), tipoSel)) continue;
                    listaView.getItems().add("📄 " + d.get("nombreOriginal").asText());
                }
            }
            listaView.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) {
                    int index = listaView.getSelectionModel().getSelectedIndex();
                    if (index >= 0 && index < carpetasActuales.size()) entrarEnCarpeta(carpetasActuales.get(index));
                }
            });
        } else {
            scrollCuadricula.setVisible(true);
            listaView.setVisible(false);
            flowPane.getChildren().clear();
            if (mostrarCarpetas) {
                for (JsonNode c : carpetasActuales) {
                    if (!pasaFiltro(c.get("nombre").asText(), busqueda, c, etiquetaSel)) continue;
                    flowPane.getChildren().add(crearTarjetaCarpeta(c));
                }
            }
            if (mostrarDocs) {
                for (JsonNode d : documentosActuales) {
                    if (!pasaFiltro(d.get("nombreOriginal").asText(), busqueda, d, etiquetaSel)) continue;
                    if (!pasaFiltroTipo(d.get("tipoMime").asText(), tipoSel)) continue;
                    flowPane.getChildren().add(crearTarjetaDocumento(d));
                }
            }
        }
    }

    // FILTROS

    private boolean pasaFiltro(String nombre, String busqueda, JsonNode nodo, String etiqueta) {
        return (busqueda.isEmpty() || nombre.toLowerCase().contains(busqueda)) && pasaFiltroEtiqueta(nodo, etiqueta);
    }

    private boolean pasaFiltroTipo(String mime, String tipo) {
        if (tipo == null || tipo.equals("Todos los tipos")) return true;
        return switch (tipo) {
            case "PDF" -> mime.contains("pdf");
            case "Imagen" -> mime.contains("image");
            case "Word" -> mime.contains("word") || mime.contains("document");
            case "Excel" -> mime.contains("excel") || mime.contains("sheet");
            default -> true;
        };
    }

    private boolean pasaFiltroEtiqueta(JsonNode nodo, String etiqueta) {
        if (etiqueta == null || etiqueta.equals("Todas las etiquetas")) return true;
        return tieneEtiqueta(nodo) && nodo.get("etiqueta").get("nombre").asText().equals(etiqueta);
    }

    private boolean tieneEtiqueta(JsonNode nodo) {
        return nodo.has("etiqueta") && !nodo.get("etiqueta").isNull();
    }

    @FXML public void aplicarFiltros() {
        String busqueda = campoBusqueda.getText().toLowerCase().trim();
        if (busquedaGlobal && !busqueda.isEmpty()) buscarGlobal(busqueda);
        else renderizar();
    }

    private void buscarGlobal(String busqueda) {
        String tipoSel = filtroTipo.getValue();
        String etiquetaSel = filtroEtiqueta.getValue();
        boolean mostrarCarpetas = filtroTodos.isSelected() || filtroCarpetas.isSelected();
        boolean mostrarDocs = filtroTodos.isSelected() || filtroDocumentos.isSelected();

        final List<JsonNode> carpRes = new ArrayList<>();
        final List<JsonNode> docRes = new ArrayList<>();

        AsyncTask.ejecutar(
                () -> {
                    if (mostrarCarpetas) carpRes.addAll(CarpetaService.buscar(busqueda));
                    if (mostrarDocs) docRes.addAll(DocumentoService.buscar(busqueda));
                },
                () -> {
                    flowPane.getChildren().clear();
                    for (JsonNode c : carpRes)
                        if (pasaFiltroEtiqueta(c, etiquetaSel)) flowPane.getChildren().add(crearTarjetaCarpeta(c));
                    for (JsonNode d : docRes) {
                        if (!pasaFiltroTipo(d.get("tipoMime").asText(), tipoSel)) continue;
                        if (!pasaFiltroEtiqueta(d, etiquetaSel)) continue;
                        flowPane.getChildren().add(crearTarjetaDocumento(d));
                    }
                }
        );
    }

    @FXML public void limpiarFiltros() {
        campoBusqueda.clear();
        filtroTipo.getSelectionModel().selectFirst();
        filtroEtiqueta.getSelectionModel().selectFirst();
        filtroTodos.setSelected(true);
        renderizar();
    }

    @FXML public void cambiarVista() {
        vistaLista = toggleVista.isSelected();
        toggleVista.setText(vistaLista ? "☰" : "⊞");
        renderizar();
    }

    @FXML public void cambiarModoBusqueda() {
        busquedaGlobal = toggleBusquedaGlobal.isSelected();
        toggleBusquedaGlobal.setText(busquedaGlobal ? "🌐 Global" : "📁 Carpeta");
        toggleBusquedaGlobal.setStyle(busquedaGlobal ? TOGGLE_ACTIVO : TOGGLE_INACTIVO);
        aplicarFiltros();
    }

    // TARJETAS

    private VBox crearTarjetaCarpeta(JsonNode carpeta) {
        String nombre = carpeta.get("nombre").asText();
        int id = carpeta.get("id").asInt();
        String color = tieneEtiqueta(carpeta) ? carpeta.get("etiqueta").get("color").asText() : "#f6c90e";
        String nombreEtq = tieneEtiqueta(carpeta) ? carpeta.get("etiqueta").get("nombre").asText() : "";

        VBox tarjeta = crearTarjetaBase("📁", color, nombre, nombreEtq);
        tarjeta.setOnMouseClicked(e -> { if (e.getClickCount() == 2) entrarEnCarpeta(carpeta); });

        ContextMenu menu = new ContextMenu();
        menu.getItems().addAll(
                menuItem("🏷️ Asignar etiqueta", () -> asignarEtiquetaCarpeta(id)),
                menuItem("✖ Quitar etiqueta", () -> quitarEtiquetaCarpeta(id)),
                new SeparatorMenuItem(),
                menuItem("🗑 Eliminar carpeta", () -> eliminarCarpeta(id, nombre)));
        tarjeta.setOnContextMenuRequested(e -> menu.show(tarjeta, e.getScreenX(), e.getScreenY()));
        return tarjeta;
    }

    private VBox crearTarjetaDocumento(JsonNode doc) {
        String nombre = doc.get("nombreOriginal").asText();
        String tipo = doc.get("tipoMime").asText();
        int idDoc = doc.get("id").asInt();
        String color = tieneEtiqueta(doc) ? doc.get("etiqueta").get("color").asText() : "#e2e8f0";
        String nombreEtq = tieneEtiqueta(doc) ? doc.get("etiqueta").get("nombre").asText() : "";

        String icono = tipo.contains("image") ? "🖼" : tipo.contains("pdf") ? "📕"
                : (tipo.contains("word") || tipo.contains("document")) ? "📝"
                : (tipo.contains("excel") || tipo.contains("sheet")) ? "📊" : "📄";

        VBox tarjeta = crearTarjetaBase(icono, color, nombre, nombreEtq);
        tarjeta.setOnMouseClicked(e -> { if (e.getClickCount() == 2) abrirDocumento(doc); });

        ContextMenu menu = new ContextMenu();
        menu.getItems().addAll(
                menuItem("📂 Abrir documento", () -> abrirDocumento(doc)),
                new SeparatorMenuItem(),
                menuItem("🏷️ Asignar etiqueta", () -> asignarEtiquetaDocumento(idDoc)),
                menuItem("✖ Quitar etiqueta", () -> quitarEtiquetaDocumento(idDoc)),
                new SeparatorMenuItem(),
                menuItem("🗑 Eliminar documento", () -> eliminarDocumento(idDoc, nombre)));
        tarjeta.setOnContextMenuRequested(e -> menu.show(tarjeta, e.getScreenX(), e.getScreenY()));
        return tarjeta;
    }

    private VBox crearTarjetaBase(String icono, String color, String nombre, String etiqueta) {
        VBox iconoBox = new VBox();
        iconoBox.setAlignment(Pos.CENTER);
        iconoBox.setPrefSize(80, 60);
        iconoBox.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 8px;");
        iconoBox.getChildren().add(new Label(icono) {{ setStyle("-fx-font-size: 28px;"); }});

        Label nombreLabel = new Label(nombre);
        nombreLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        nombreLabel.setMaxWidth(90); nombreLabel.setWrapText(true);

        Label etqLabel = new Label(etiqueta.isEmpty() ? "" : "🏷️ " + etiqueta);
        etqLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #718096;");

        VBox tarjeta = new VBox(5, iconoBox, nombreLabel, etqLabel);
        tarjeta.setAlignment(Pos.CENTER);
        tarjeta.setPrefWidth(100);
        tarjeta.setPadding(new Insets(10));
        tarjeta.setStyle(TARJETA_NORMAL);
        hoverTarjeta(tarjeta);
        return tarjeta;
    }

    private MenuItem menuItem(String texto, Runnable accion) {
        MenuItem item = new MenuItem(texto);
        item.setOnAction(e -> accion.run());
        return item;
    }

    // ABRIR DOCUMENTO

    private void abrirDocumento(JsonNode doc) {
        int idDoc = doc.get("id").asInt();
        String nombreOriginal = doc.get("nombreOriginal").asText();
        AsyncTask.ejecutar(
                () -> {
                    byte[] contenido = DocumentoService.descargar(idDoc);
                    java.nio.file.Path tempDir = java.nio.file.Files.createTempDirectory("arkaly_");
                    java.nio.file.Path tempFile = tempDir.resolve(nombreOriginal);
                    java.nio.file.Files.write(tempFile, contenido);
                    javafx.application.Platform.runLater(() -> {
                        try { java.awt.Desktop.getDesktop().open(tempFile.toFile()); }
                        catch (Exception e) { new Alert(Alert.AlertType.ERROR, "No se pudo abrir: " + e.getMessage()).showAndWait(); }
                    });
                },
                () -> {}
        );
    }

    // NAVEGACIÓN

    private void entrarEnCarpeta(JsonNode carpeta) {
        historialIds.add(carpetaActualId);
        historialNombres.add(rutaLabel.getText());
        carpetaActualId = carpeta.get("id").asInt();
        btnAtras.setDisable(false);
        resetearBusqueda();
        cargarCarpetas(carpetaActualId, carpeta.get("nombre").asText());
    }

    @FXML public void navegarAtras() {
        if (historialIds.isEmpty()) return;
        carpetaActualId = historialIds.remove(historialIds.size() - 1);
        String nombreAnterior = historialNombres.remove(historialNombres.size() - 1);
        btnAtras.setDisable(historialIds.isEmpty());
        resetearBusqueda();
        cargarCarpetas(carpetaActualId, nombreAnterior);
    }

    private void resetearBusqueda() {
        campoBusqueda.clear();
        busquedaGlobal = false;
        toggleBusquedaGlobal.setSelected(false);
        toggleBusquedaGlobal.setText("📁 Carpeta");
        toggleBusquedaGlobal.setStyle(TOGGLE_INACTIVO);
    }

    // SUBIR DOCUMENTO

    @FXML public void subirDocumento() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Seleccionar documento");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Documentos", "*.pdf", "*.png", "*.jpg", "*.jpeg", "*.docx", "*.xlsx"));
        File archivo = fc.showOpenDialog((Stage) flowPane.getScene().getWindow());
        if (archivo == null) return;

        TextInputDialog dialog = new TextInputDialog("General");
        dialog.setTitle("Categoría");
        dialog.setHeaderText("¿Qué categoría tiene este documento?");
        dialog.setContentText("Categoría:");

        dialog.showAndWait().ifPresent(categoria -> AsyncTask.ejecutar(
                () -> DocumentoService.subir(archivo.toPath(), categoria.trim(), carpetaActualId),
                () -> cargarCarpetas(carpetaActualId, rutaLabel.getText())
        ));
    }

    // REAR CARPETA / ETIQUETA

    @FXML public void crearCarpeta() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nueva Carpeta");
        dialog.setHeaderText("Crear nueva carpeta");
        dialog.setContentText("Nombre:");
        dialog.showAndWait().ifPresent(nombre -> {
            if (nombre.trim().isEmpty()) return;
            AsyncTask.ejecutar(
                    () -> CarpetaService.crear(nombre.trim(), carpetaActualId),
                    () -> cargarCarpetas(carpetaActualId, rutaLabel.getText()));
        });
    }

    @FXML public void crearEtiqueta() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Nueva Etiqueta");
        dialog.setHeaderText("Crear nueva etiqueta");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));

        TextField nombreField = new TextField();
        nombreField.setPromptText("Nombre de la etiqueta");

        List<String> colores = List.of("#FF6B6B", "#FFA500", "#FFD93D", "#6BCB77", "#4D96FF", "#C77DFF");
        HBox colorBox = new HBox(8);
        ToggleGroup colorGroup = new ToggleGroup();
        final String[] colorSel = {colores.get(0)};

        for (String c : colores) {
            ToggleButton btn = new ToggleButton();
            btn.setToggleGroup(colorGroup);
            btn.setPrefSize(30, 30);
            btn.setStyle("-fx-background-color: " + c + "; -fx-background-radius: 15px;");
            btn.setUserData(c);
            colorBox.getChildren().add(btn);
        }
        ((ToggleButton) colorBox.getChildren().get(0)).setSelected(true);

        colorGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            colorBox.getChildren().forEach(node -> {
                ToggleButton btn = (ToggleButton) node;
                btn.setStyle("-fx-background-color: " + btn.getUserData() + "; -fx-background-radius: 15px;");
            });
            if (newVal != null) {
                colorSel[0] = (String) newVal.getUserData();
                ((ToggleButton) newVal).setStyle("-fx-background-color: " + colorSel[0] +
                        "; -fx-background-radius: 15px; -fx-border-color: white; -fx-border-width: 3px; " +
                        "-fx-border-radius: 15px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 6, 0, 0, 0);");
            }
        });

        grid.add(new Label("Nombre:"), 0, 0); grid.add(nombreField, 1, 0);
        grid.add(new Label("Color:"), 0, 1); grid.add(colorBox, 1, 1);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK && !nombreField.getText().trim().isEmpty())
                AsyncTask.ejecutar(
                        () -> EtiquetaService.crear(nombreField.getText().trim(), colorSel[0]),
                        this::cargarEtiquetas);
        });
    }

    // ETIQUETAS

    private void asignarEtiquetaCarpeta(int id) {
        elegirEtiqueta().ifPresent(idEtq -> AsyncTask.ejecutar(
                () -> EtiquetaService.asignarACarpeta(id, idEtq),
                () -> cargarCarpetas(carpetaActualId, rutaLabel.getText())));
    }

    private void quitarEtiquetaCarpeta(int id) {
        AsyncTask.ejecutar(() -> EtiquetaService.quitarDeCarpeta(id),
                () -> cargarCarpetas(carpetaActualId, rutaLabel.getText()));
    }

    private void asignarEtiquetaDocumento(int id) {
        elegirEtiqueta().ifPresent(idEtq -> AsyncTask.ejecutar(
                () -> EtiquetaService.asignarADocumento(id, idEtq),
                () -> cargarCarpetas(carpetaActualId, rutaLabel.getText())));
    }

    private void quitarEtiquetaDocumento(int id) {
        AsyncTask.ejecutar(() -> EtiquetaService.quitarDeDocumento(id),
                () -> cargarCarpetas(carpetaActualId, rutaLabel.getText()));
    }

    private java.util.Optional<Integer> elegirEtiqueta() {
        if (etiquetasDisponibles.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "No tienes etiquetas. Crea una primero.").showAndWait();
            return java.util.Optional.empty();
        }
        List<String> nombres = new ArrayList<>();
        for (JsonNode e : etiquetasDisponibles) nombres.add(e.get("nombre").asText());
        ChoiceDialog<String> dialog = new ChoiceDialog<>(nombres.get(0), nombres);
        dialog.setTitle("Asignar Etiqueta");
        dialog.setHeaderText("Selecciona una etiqueta");
        return dialog.showAndWait().map(sel -> etiquetasDisponibles.get(nombres.indexOf(sel)).get("id").asInt());
    }

    // ELIMINAR

    private void eliminarCarpeta(int id, String nombre) {
        confirmar(nombre, () -> AsyncTask.ejecutar(
                () -> CarpetaService.eliminar(id),
                () -> cargarCarpetas(carpetaActualId, rutaLabel.getText())));
    }

    private void eliminarDocumento(int id, String nombre) {
        confirmar(nombre, () -> AsyncTask.ejecutar(
                () -> DocumentoService.eliminar(id),
                () -> cargarCarpetas(carpetaActualId, rutaLabel.getText())));
    }

    private void confirmar(String nombre, Runnable accion) {
        Alert c = new Alert(Alert.AlertType.CONFIRMATION);
        c.setTitle("Eliminar"); c.setHeaderText("¿Eliminar " + nombre + "?");
        c.setContentText("Esta acción no se puede deshacer.");
        c.showAndWait().ifPresent(r -> { if (r == ButtonType.OK) accion.run(); });
    }
}