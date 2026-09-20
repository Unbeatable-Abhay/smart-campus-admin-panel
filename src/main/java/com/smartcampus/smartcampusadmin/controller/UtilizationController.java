package com.smartcampus.smartcampusadmin.controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.smartcampus.smartcampusadmin.MainApp;
import com.smartcampus.smartcampusadmin.api.ApiClient;
import com.smartcampus.smartcampusadmin.api.ApiResponse;
import com.smartcampus.smartcampusadmin.session.SessionManager;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ListView;

public class UtilizationController {

    @FXML
    private BarChart<String, Number> resourceChart;

    @FXML
    private ListView<String> clustersListView;

    @FXML
    public void initialize() {
        loadUtilization();
        loadClusters();
    }

    private void loadUtilization() {
        Thread fetchThread = new Thread(() -> {
            try {
                ApiResponse response = ApiClient.getUtilization();

                if (response.getStatusCode() == 200) {
                    JsonObject json = JsonParser.parseString(response.getBody()).getAsJsonObject();
                    JsonArray mostBooked = json.getAsJsonArray("most_booked_resources");

                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    series.setName("Bookings");

                    for (int i = 0; i < mostBooked.size(); i++) {
                        JsonObject entry = mostBooked.get(i).getAsJsonObject();
                        String resourceId = entry.get("resource_id").getAsString();
                        int count = entry.get("booking_count").getAsInt();
                        series.getData().add(new XYChart.Data<>(resourceId, count));
                    }

                    Platform.runLater(() -> {
                        resourceChart.getData().clear();
                        resourceChart.getData().add(series);
                    });
                }
            } catch (Exception e) {
                // Chart just stays empty on failure; not critical enough to block the screen
            }
        });

        fetchThread.setDaemon(true);
        fetchThread.start();
    }

    private void loadClusters() {
        Thread fetchThread = new Thread(() -> {
            try {
                ApiResponse response = ApiClient.getClusters();

                if (response.getStatusCode() == 200) {
                    JsonArray clusters = JsonParser.parseString(response.getBody()).getAsJsonArray();

                    java.util.List<String> displayLines = new java.util.ArrayList<>();

                    for (int i = 0; i < clusters.size(); i++) {
                        JsonObject entry = clusters.get(i).getAsJsonObject();
                        int resourceId = entry.get("resource_id").getAsInt();
                        int bookingCount = entry.get("booking_count").getAsInt();
                        int cluster = entry.get("cluster").getAsInt();

                        displayLines.add("Resource #" + resourceId + " — Bookings: " + bookingCount + " — Cluster: " + cluster);
                    }

                    Platform.runLater(() -> clustersListView.setItems(FXCollections.observableArrayList(displayLines)));
                }
            } catch (Exception e) {
                // ListView just stays empty on failure
            }
        });

        fetchThread.setDaemon(true);
        fetchThread.start();
    }

    @FXML
    private void handleGoToBookings() {
        switchScene("bookings.fxml");
    }

    @FXML
    private void handleGoToResources() {
        switchScene("resources.fxml");
    }

    @FXML
    private void handleLogout() {
        SessionManager.clear();
        switchScene("login.fxml");
    }

    private void switchScene(String fxmlFile) {
        try {
            MainApp.switchScene(fxmlFile);
        } catch (Exception e) {
            // Scene switch failure would need a full app restart to recover from; acceptable for now
        }
    }
}