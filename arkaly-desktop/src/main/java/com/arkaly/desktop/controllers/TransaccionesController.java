package com.arkaly.desktop.controllers;

import static com.arkaly.desktop.utils.Estilos.*;

import com.arkaly.desktop.services.InformeService;
import com.arkaly.desktop.utils.AsyncTask;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.*;

public class TransaccionesController {

    @FXML private TableView<Map<String, Object>> tablaTransacciones;
    @FXML private TableColumn<Map<String, Object>, String> colTxFecha, colTxCategoria, colTxTipo, colTxCantidad, colTxDescripcion;
    @FXML private TableColumn<Map<String, Object>, Void> colTxAcciones;

    @FXML public void initialize() {
        configurarTabla();
        cargar(LocalDate.now().withDayOfMonth(1).toString(), LocalDate.now().toString());
    }

    public void cargar(String desde, String hasta) {
        AsyncTask.ejecutar(
                () -> {
                    JsonNode arr = InformeService.getTransacciones(desde, hasta);
                    List<Map<String, Object>> lista = new ArrayList<>();
                    for (JsonNode n : arr) {
                        Map<String, Object> row = new LinkedHashMap<>();
                        row.put("fecha", n.get("fecha").asText());
                        row.put("categoria", n.has("nombreCategoria") && !n.get("nombreCategoria").isNull() ? n.get("nombreCategoria").asText() : "-");
                        row.put("tipo", n.get("tipo").asText());
                        row.put("cantidad", String.format("%.2f €", n.get("cantidad").asDouble()));
                        row.put("descripcion", n.has("descripcion") && !n.get("descripcion").isNull() ? n.get("descripcion").asText() : "");
                        row.put("id", n.get("id").asInt());
                        lista.add(row);
                    }
                    Platform.runLater(() -> tablaTransacciones.setItems(FXCollections.observableArrayList(lista)));
                },
                () -> {}
        );
    }

    @FXML public void abrirNuevaTransaccion() {
        final List<Map<String, Object>> cats = new ArrayList<>();
        AsyncTask.ejecutar(
                () -> {
                    JsonNode arr = InformeService.getCategorias();
                    for (JsonNode n : arr) {
                        Map<String, Object> c = new LinkedHashMap<>();
                        c.put("id", n.get("id").asInt());
                        c.put("nombre", n.get("nombre").asText());
                        cats.add(c);
                    }
                },
                () -> {
                    if (cats.isEmpty()) {
                        new Alert(Alert.AlertType.WARNING, "Crea primero una categoría con el botón ⚙ Categorías.").showAndWait();
                        return;
                    }
                    mostrarDialogoTransaccion(cats);
                }
        );
    }

    private void mostrarDialogoTransaccion(List<Map<String, Object>> cats) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Nueva Transacción");

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(12); grid.setStyle("-fx-padding: 20;");

        ComboBox<String> cmbTipo = new ComboBox<>();
        cmbTipo.getItems().addAll("GASTO", "INGRESO"); cmbTipo.setValue("GASTO");

        ComboBox<Map<String, Object>> cmbCat = new ComboBox<>();
        cmbCat.setItems(FXCollections.observableArrayList(cats));
        cmbCat.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Map<String, Object> item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : (String) item.get("nombre"));
            }
        });
        cmbCat.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Map<String, Object> item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : (String) item.get("nombre"));
            }
        });
        cmbCat.getSelectionModel().selectFirst(); cmbCat.setPrefWidth(200);

        TextField txtCant = new TextField(); txtCant.setPromptText("0.00");
        TextField txtDesc = new TextField(); txtDesc.setPromptText("Descripción opcional");
        DatePicker dpFecha = new DatePicker(LocalDate.now());

        grid.addRow(0, new Label("Tipo:"), cmbTipo);
        grid.addRow(1, new Label("Categoría:"), cmbCat);
        grid.addRow(2, new Label("Cantidad (€):"), txtCant);
        grid.addRow(3, new Label("Descripción:"), txtDesc);
        grid.addRow(4, new Label("Fecha:"), dpFecha);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setMinWidth(350);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                Map<String, Object> cat = cmbCat.getValue();
                if (cat == null) return;
                AsyncTask.ejecutar(
                        () -> InformeService.guardarTransaccion(cmbTipo.getValue(), (Integer) cat.get("id"),
                                txtCant.getText(), txtDesc.getText(), dpFecha.getValue()),
                        () -> cargar(LocalDate.now().withDayOfMonth(1).toString(), LocalDate.now().toString())
                );
            }
        });
    }

    @FXML public void abrirGestionCategorias() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Gestión de Categorías");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setMinWidth(420); dialog.getDialogPane().setPrefHeight(550);

        VBox contenido = new VBox(12); contenido.setStyle("-fx-padding: 20;"); contenido.setPrefWidth(380);

        Label lblNueva = new Label("Nueva categoría:"); lblNueva.setStyle("-fx-font-weight: bold; -fx-text-fill: #2d3748;");
        HBox crearBox = new HBox(8); crearBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        TextField txtNueva = new TextField(); txtNueva.setPromptText("Nombre de la categoría...");
        HBox.setHgrow(txtNueva, Priority.ALWAYS);
        Button btnCrear = new Button("+ Añadir"); btnCrear.setStyle(BTN_VERDE);
        crearBox.getChildren().addAll(txtNueva, btnCrear);

        Label lblExist = new Label("Categorías existentes:"); lblExist.setStyle("-fx-font-weight: bold; -fx-text-fill: #2d3748;");
        VBox listaCats = new VBox(8); listaCats.setStyle("-fx-padding: 4 0 4 0;");
        ScrollPane scrollLista = new ScrollPane(listaCats);
        scrollLista.setFitToWidth(true); scrollLista.setPrefHeight(300);
        scrollLista.setStyle("-fx-background-color: transparent; -fx-border-color: #e2e8f0; -fx-border-radius: 8px;");

        Runnable[] recargar = new Runnable[1];
        recargar[0] = () -> AsyncTask.ejecutar(
                () -> InformeService.getCategorias(),
                () -> {} // se ejecuta en el hilo background, necesitamos el resultado
        );
        // Versión correcta con acceso al resultado:
        recargar[0] = () -> {
            final JsonNode[] arr = {null};
            AsyncTask.ejecutar(
                    () -> arr[0] = InformeService.getCategorias(),
                    () -> {
                        listaCats.getChildren().clear();
                        if (arr[0] == null || arr[0].isEmpty()) {
                            listaCats.getChildren().add(new Label("No hay categorías aún.") {{ setStyle("-fx-text-fill: #718096; -fx-padding: 10;"); }});
                            return;
                        }
                        for (JsonNode n : arr[0]) {
                            int id = n.get("id").asInt();
                            String nombre = n.get("nombre").asText();
                            HBox fila = new HBox(8); fila.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                            fila.setStyle("-fx-background-color: #f7fafc; -fx-background-radius: 6px; -fx-padding: 8 12 8 12;");
                            Label lblNom = new Label(nombre); lblNom.setStyle("-fx-font-size: 13px; -fx-text-fill: #2d3748;");
                            HBox.setHgrow(lblNom, Priority.ALWAYS);
                            Button btnElim = new Button("🗑"); btnElim.setStyle(BTN_ELIMINAR);
                            btnElim.setOnAction(e -> AsyncTask.ejecutar(() -> InformeService.eliminarCategoria(id), () -> recargar[0].run()));
                            fila.getChildren().addAll(lblNom, btnElim);
                            listaCats.getChildren().add(fila);
                        }
                    }
            );
        };

        btnCrear.setOnAction(e -> {
            String nombre = txtNueva.getText().trim();
            if (nombre.isBlank()) return;
            AsyncTask.ejecutar(() -> InformeService.crearCategoria(nombre), () -> { txtNueva.clear(); recargar[0].run(); });
        });
        txtNueva.setOnAction(e -> btnCrear.fire());
        recargar[0].run();

        contenido.getChildren().addAll(lblNueva, crearBox, new Separator(), lblExist, scrollLista);
        dialog.getDialogPane().setContent(contenido);
        dialog.showAndWait();
    }

    private void configurarTabla() {
        colTxFecha.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty((String) d.getValue().get("fecha")));
        colTxCategoria.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty((String) d.getValue().get("categoria")));
        colTxTipo.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty((String) d.getValue().get("tipo")));
        colTxCantidad.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty((String) d.getValue().get("cantidad")));
        colTxDescripcion.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty((String) d.getValue().get("descripcion")));

        colTxAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("🗑") {{ setStyle(BTN_ELIMINAR);
                setOnAction(e -> {
                    Map<String, Object> row = getTableView().getItems().get(getIndex());
                    AsyncTask.ejecutar(() -> InformeService.eliminarTransaccion((Integer) row.get("id")),
                            () -> cargar(LocalDate.now().withDayOfMonth(1).toString(), LocalDate.now().toString()));
                });
            }};
            @Override protected void updateItem(Void v, boolean empty) { super.updateItem(v, empty); setGraphic(empty ? null : btn); }
        });

        tablaTransacciones.setRowFactory(tv -> new TableRow<>() {
            { selectedProperty().addListener((obs, old, sel) -> actualizarEstilo()); }
            @Override protected void updateItem(Map<String, Object> item, boolean empty) { super.updateItem(item, empty); actualizarEstilo(); }
            private void actualizarEstilo() {
                Map<String, Object> item = getItem();
                if (isEmpty() || item == null || isSelected()) setStyle("");
                else setStyle("INGRESO".equals(item.get("tipo")) ? "-fx-background-color: #f0fff4;" : "-fx-background-color: #fff5f5;");
            }
        });
    }
}