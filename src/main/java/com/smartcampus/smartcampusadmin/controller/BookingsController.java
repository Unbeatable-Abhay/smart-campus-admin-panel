package com.smartcampus.smartcampusadmin.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.smartcampus.smartcampusadmin.MainApp;
import com.smartcampus.smartcampusadmin.api.ApiClient;
import com.smartcampus.smartcampusadmin.api.ApiResponse;
import com.smartcampus.smartcampusadmin.model.Booking;
import com.smartcampus.smartcampusadmin.session.SessionManager;
import com.smartcampus.smartcampusadmin.session.SessionManager;

import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;

import java.lang.reflect.Type;
import java.util.List;

public class BookingsController {

    @FXML
    private TableView<Booking> bookingsTable;

    @FXML
    private TableColumn<Booking, Integer> idColumn;

    @FXML
    private TableColumn<Booking, Integer> resourceIdColumn;

    @FXML
    private TableColumn<Booking, Integer> userIdColumn;

    @FXML
    private TableColumn<Booking, String> startTimeColumn;

    @FXML
    private TableColumn<Booking, String> endTimeColumn;

    @FXML
    private TableColumn<Booking, String> statusColumn;

    @FXML
    private ComboBox<String> statusFilterBox;

    @FXML
    private VBox sidebarPanel;

    @FXML
    private Button avatarButton;

    @FXML
    private void handleToggleSidebar() {
        boolean isCurrentlyOpen = sidebarPanel.isVisible();
        sidebarPanel.setVisible(!isCurrentlyOpen);
        sidebarPanel.setManaged(!isCurrentlyOpen);
    }

    private Gson gson = new Gson();
    private ObservableList<Booking> allBookings = FXCollections.observableArrayList();
    private Timeline pollingTimeline;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        resourceIdColumn.setCellValueFactory(new PropertyValueFactory<>("resourceId"));
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        startTimeColumn.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        endTimeColumn.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        statusFilterBox.getItems().addAll("All", "pending", "approved", "rejected", "cancelled");
        statusFilterBox.setValue("All");
        statusFilterBox.setOnAction(event -> applyFilter());

        String userName = SessionManager.getUserName();
        String initial = (userName != null && !userName.isEmpty()) ? userName.substring(0, 1).toUpperCase() : "A";
        avatarButton.setText(initial);

        loadBookings();
        startPolling();
    }

    private void startPolling() {
        pollingTimeline = new Timeline(new KeyFrame(Duration.seconds(3), event -> loadBookings()));
        pollingTimeline.setCycleCount(Timeline.INDEFINITE);
        pollingTimeline.play();
    }

    private void loadBookings() {
        Thread fetchThread = new Thread(() -> {
            try {
                ApiResponse response = ApiClient.getBookings();

                if (response.getStatusCode() == 200) {
                    Type listType = new TypeToken<List<Booking>>() {}.getType();
                    List<Booking> bookings = gson.fromJson(response.getBody(), listType);

                    Platform.runLater(() -> {
                        allBookings.setAll(bookings);
                        applyFilter();
                    });
                }
            } catch (Exception e) {
                // Silently skip this poll cycle on network failure; next poll will retry automatically
            }
        });

        fetchThread.setDaemon(true);
        fetchThread.start();
    }

    private void applyFilter() {
        String selectedStatus = statusFilterBox.getValue();

        if (selectedStatus == null || selectedStatus.equals("All")) {
            bookingsTable.setItems(allBookings);
        } else {
            ObservableList<Booking> filtered = FXCollections.observableArrayList();
            for (Booking booking : allBookings) {
                if (booking.getStatus().equals(selectedStatus)) {
                    filtered.add(booking);
                }
            }
            bookingsTable.setItems(filtered);
        }
    }

    @FXML
    private void handleApprove() {
        updateSelectedBookingStatus("approved");
    }

    @FXML
    private void handleReject() {
        updateSelectedBookingStatus("rejected");
    }

    private void updateSelectedBookingStatus(String newStatus) {
        Booking selected = bookingsTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            return;
        }

        Thread updateThread = new Thread(() -> {
            try {
                ApiClient.updateBookingStatus(selected.getId(), newStatus);
                loadBookings();
            } catch (Exception e) {
                // If this fails, the next poll cycle will just show the unchanged status
            }
        });

        updateThread.setDaemon(true);
        updateThread.start();
    }

    @FXML
    private void handleGoToUtilization() {
        stopPollingAndSwitch("utilization.fxml");
    }

    @FXML
    private void handleGoToResources() {
        stopPollingAndSwitch("resources.fxml");
    }

    @FXML
    private void handleGoToBookings() {
        // Already on this screen — button exists for sidebar consistency, no action needed
    }

    @FXML
    private void handleLogout() {
        SessionManager.clear();
        stopPollingAndSwitch("login.fxml");
    }

    private void stopPollingAndSwitch(String fxmlFile) {
        if (pollingTimeline != null) {
            pollingTimeline.stop();
        }

        try {
            MainApp.switchScene(fxmlFile);
        } catch (Exception e) {
            // Scene switch failure would need a full app restart to recover from; acceptable for now
        }
    }
}