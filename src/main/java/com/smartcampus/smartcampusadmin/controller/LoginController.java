package com.smartcampus.smartcampusadmin.controller;

import com.google.gson.Gson;
import com.smartcampus.smartcampusadmin.MainApp;
import com.smartcampus.smartcampusadmin.api.ApiClient;
import com.smartcampus.smartcampusadmin.api.ApiResponse;
import com.smartcampus.smartcampusadmin.model.ErrorResponse;
import com.smartcampus.smartcampusadmin.model.LoginResponse;
import com.smartcampus.smartcampusadmin.session.SessionManager;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    private Gson gson = new Gson();

    @FXML
    private void handleLogin() {
        errorLabel.setText("");

        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Email and password are required");
            return;
        }

        Thread loginThread = new Thread(() -> {
            try {
                ApiResponse response = ApiClient.login(email, password);
                handleLoginResponse(response);
            } catch (Exception e) {
                Platform.runLater(() -> errorLabel.setText("Network error: could not reach server"));
            }
        });

        loginThread.setDaemon(true);
        loginThread.start();
    }

    private void handleLoginResponse(ApiResponse response) {
        if (response.getStatusCode() == 200) {
            LoginResponse loginData = gson.fromJson(response.getBody(), LoginResponse.class);

            SessionManager.setToken(loginData.getToken());
            SessionManager.setUserName(loginData.getUser().getName());
            SessionManager.setUserRole(loginData.getUser().getRole());

            Platform.runLater(() -> {
                try {
                    MainApp.switchScene("bookings.fxml");
                } catch (Exception e) {
                    errorLabel.setText("Could not load bookings screen");
                }
            });

        } else {
            ErrorResponse errorData = gson.fromJson(response.getBody(), ErrorResponse.class);

            Platform.runLater(() -> errorLabel.setText(errorData.getError()));
        }
    }
}