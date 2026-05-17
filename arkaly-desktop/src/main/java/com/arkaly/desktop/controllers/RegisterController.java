package com.arkaly.desktop.controllers;

import com.arkaly.desktop.utils.ApiClient;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RegisterController {

    @FXML private TextField nombreField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisible;
    @FXML private Label errorLabel;
    @FXML private CheckBox checkPassword;

    @FXML
    public void initialize() {
        passwordVisible.textProperty().bindBidirectional(passwordField.textProperty());
        passwordVisible.setVisible(false);
        passwordVisible.setManaged(false);
    }

    @FXML
    public void pulsarCheckContrasenia() {
        boolean shown = checkPassword.isSelected();

        passwordField.setVisible(!shown);
        passwordField.setManaged(!shown);
        passwordVisible.setVisible(shown);
        passwordVisible.setManaged(shown);
    }

    @FXML
    public void pulsarRegistrarse() {
        String nombre = nombreField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (nombre.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Rellena todos los campos");
            return;
        }

        try {
            String json = String.format(
                    "{\"nombre\":\"%s\",\"username\":\"%s\",\"email\":\"%s\",\"password\":\"%s\"}",
                    nombre, username, email, password);

            String response = ApiClient.post("/auth/registro", json);
            System.out.println("RESPUESTA BACKEND: " + response);

            if (response.contains("éxito")) {
                // Registro OK, volvemos al login
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/arkaly/desktop/Login.fxml"));
                Scene scene = new Scene(loader.load());
                Stage stage = (Stage) emailField.getScene().getWindow();
                stage.setScene(scene);
            } else {
                errorLabel.setText(response); // Muestra el mensaje de error del backend
            }
        } catch (Exception e) {
            errorLabel.setText("Error de conexión con el servidor");
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void pulsarYaTengoCuenta() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/arkaly/desktop/Login.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            System.out.println("ERROR al abrir login: " + e.getMessage());
            e.printStackTrace();
        }
    }
}