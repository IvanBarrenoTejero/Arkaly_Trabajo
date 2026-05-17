package com.arkaly.desktop.controllers;

import com.arkaly.desktop.utils.ApiClient;
import com.arkaly.desktop.utils.SessionManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;

    private final ObjectMapper mapper = new ObjectMapper();

    @FXML
    public void handleLogin() {
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
            System.out.println("RESPUESTA BACKEND: " + response); // añade esta línea

            JsonNode node = mapper.readTree(response); // readTree convierte el JSON en un arbol navegable de nodos

            if (node.has("token")) {
                SessionManager.setToken(node.get("token").asText());

                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource(
                                "/com/arkaly/desktop/Dashboard.fxml"));
                Scene scene = new Scene(loader.load(), 1200, 700);
                Stage stage = (Stage) emailField.getScene().getWindow();
                stage.setScene(scene);
            } else {
                errorLabel.setText("Email o contraseña incorrectos");
            }
        } catch (Exception e) {
            errorLabel.setText("Error de conexión con el servidor");
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}