package com.arkaly.desktop.controllers;

import com.arkaly.desktop.utils.ApiClient;
import com.arkaly.desktop.utils.SessionManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisible;
    @FXML private Label errorLabel;
    @FXML private CheckBox checkPassword;

    private final ObjectMapper mapper = new ObjectMapper();

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
    public void pulsarIniciarSesion() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Rellena todos los campos");
            return;
        }

        try {
            String json = String.format(
                    "{\"email\":\"%s\",\"password\":\"%s\"}", email, password);
            String response = ApiClient.post("/auth/login", json);
            System.out.println("RESPUESTA BACKEND: " + response);

            JsonNode node = mapper.readTree(response);

            if (node.has("token")) {
                SessionManager.setToken(node.get("token").asText());
                System.out.println(SessionManager.getToken());

                if (node.has("id")) {
                    SessionManager.setIdUsuario(node.get("id").asInt());
                    System.out.println(SessionManager.getIdUsuario());
                }
                if (node.has("nombre")) {
                    SessionManager.setNombreUsuario(node.get("nombre").asText());
                }

                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource(
                                "/com/arkaly/desktop/Dashboard.fxml"));
                Scene scene = new Scene(loader.load());
                Stage stage = (Stage) emailField.getScene().getWindow();
                stage.setScene(scene);
                stage.setMaximized(true);
            } else {
                errorLabel.setText("Email o contraseña incorrectos");
            }
        } catch (Exception e) {
            errorLabel.setText("Error de conexión con el servidor");
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void pulsarCrearCuenta() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/com/arkaly/desktop/Register.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            System.out.println("ERROR al abrir registro: " + e.getMessage());
            e.printStackTrace();
        }
    }
}