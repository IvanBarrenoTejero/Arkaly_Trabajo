package com.arkaly.desktop.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class DashboardController {

    @FXML private StackPane contenidoPanel;

    @FXML
    public void initialize() {
        mostrarInicio();
    }

    @FXML
    public void mostrarInicio() {
        contenidoPanel.getChildren().clear();

        VBox contenido = new VBox(20);
        contenido.setAlignment(javafx.geometry.Pos.CENTER);
        contenido.setPadding(new javafx.geometry.Insets(60));

        ImageView logo = new ImageView(new Image(
                getClass().getResourceAsStream("/com/arkaly/desktop/images/logoAzul.png")));
        logo.setFitWidth(180);
        logo.setPreserveRatio(true);

        Label titulo = new Label("Bienvenido a Arkaly");
        titulo.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

        Label subtitulo = new Label("Gestiona tus inversiones de forma sencilla");
        subtitulo.setStyle("-fx-font-size: 14px; -fx-text-fill: #a1a0a0;");

        contenido.getChildren().addAll(logo, titulo, subtitulo);
        contenidoPanel.getChildren().add(contenido);
    }

    @FXML
    public void mostrarGestorCarpetas() {
        cargarVista("GestorCarpetas.fxml");
    }

    @FXML
    public void mostrarInversiones() {
        cargarVista("Inversiones_General.fxml");
    }

    @FXML
    public void mostrarInformes() {
        cargarVista("Informes.fxml");
    }

    @FXML
    public void mostrarPerfil() {cargarVista(("ProfileView.fxml"));}

    private void cargarVista(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/arkaly/desktop/" + fxml));

            javafx.scene.Parent vista = loader.load();

            contenidoPanel.getChildren().setAll(vista);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}