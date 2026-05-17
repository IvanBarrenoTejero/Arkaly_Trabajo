package com.arkaly.desktop.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

public class InformesController {

    @FXML private StackPane contenidoInformes;

    @FXML
    public void initialize() {
        // Carga gráficos por defecto al abrir
        mostrarGraficos();
    }

    @FXML
    public void mostrarGraficos() {
        cargar("Graficos.fxml");
    }

    @FXML
    public void mostrarTransacciones() {
        cargar("Transacciones.fxml");
    }

    private void cargar(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/arkaly/desktop/" + fxml));
            Node node = loader.load();
            contenidoInformes.getChildren().setAll(node);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}