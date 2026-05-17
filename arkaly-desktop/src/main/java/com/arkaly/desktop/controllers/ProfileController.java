package com.arkaly.desktop.controllers;

import com.arkaly.desktop.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ProfileController {

    @FXML private Label nombreLabel;
    @FXML private Label idLabel;

    @FXML
    public void initialize() {
        nombreLabel.setText(SessionManager.getNombreUsuario());
        idLabel.setText(SessionManager.getIdUsuario() != null
                ? SessionManager.getIdUsuario().toString()
                : "-");
    }

    @FXML
    public void pulsarCerrarSesion() {
        try {
            SessionManager.cerrarSesion();

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/arkaly/desktop/Login.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) nombreLabel.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            System.out.println("ERROR al cerrar sesión: " + e.getMessage());
            e.printStackTrace();
        }
    }
}