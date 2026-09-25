package com.smartcampus.smartcampusadmin.controller;

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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

public class UtilizationController {

    @FXML
    private BarChart<String, Number> resourceChart;

    @FXML
    private ListView<String> clustersListView;

    @FXML
    private Label totalBookingsValue;

    @FXML
    private Label topResourceValue;

    @FXML
    private Label peakHourValue;

    @FXML
    private VBox sidebarPanel;

    @FXML
    private Button avatarButton;

    @FXML
    public void initialize() {
        String userName = SessionManager.getUserName();
        String initial = (userName != null && !userName.isEmpty()) ? userName.substring(0, 1).toUpperCase() : "A";
        avatarButton.setText(initial);

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
                    JsonArray peakHours = json.getAsJsonArray("peak_hours");
                    int totalBookings = json.get("total_bookings").getAsInt();

                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    series.setName("Bookings");

                    for (int i = 0; i < mostBooked.size(); i++) {
                        JsonObject entry = mostBooked.get(i).getAsJsonObject();
                        String resourceId = entry.get("resource_id").getAsString();
                        int count = entry.get("booking_count").getAsInt();
                        series.getData().add(new XYChart.Data<>(resourceId, count));
                    }

                    String topResourceText = "-";
                    if (mostBooked.size() > 0) {
                        topResourceText = "Resource #" + mostBooked.get(0).getAsJsonObject().get("resource_id").getAsString();
                    }

                    String peakHourText = "-";
                    if (peakHours.size() > 0) {
                        peakHourText = peakHours.get(0).getAsJsonObject().get("hour").getAsString() + ":00";
                    }

                    final String finalTopResourceText = topResourceText;
                    final String finalPeakHourText = peakHourText;

                    Platform.runLater(() -> {
                        resourceChart.getData().clear();
                        resourceChart.getData().add(series);

                        totalBookingsValue.setText(String.valueOf(totalBookings));
                        topResourceValue.setText(finalTopResourceText);
                        peakHourValue.setText(finalPeakHourText);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
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
                e.printStackTrace();
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
    private void handleGoToUtilization() {
        // Already on this screen — button exists for sidebar consistency, no action needed
    }

    @FXML
    private void handleLogout() {
        SessionManager.clear();
        switchScene("login.fxml");
    }

    @FXML
    private void handleToggleSidebar() {
        boolean isCurrentlyOpen = sidebarPanel.isVisible();
        sidebarPanel.setVisible(!isCurrentlyOpen);
        sidebarPanel.setManaged(!isCurrentlyOpen);
    }

    private void switchScene(String fxmlFile) {
        try {
            MainApp.switchScene(fxmlFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}