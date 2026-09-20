package com.smartcampus.smartcampusadmin.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.smartcampus.smartcampusadmin.MainApp;
import com.smartcampus.smartcampusadmin.api.ApiClient;
import com.smartcampus.smartcampusadmin.api.ApiResponse;
import com.smartcampus.smartcampusadmin.model.Resource;
import com.smartcampus.smartcampusadmin.session.SessionManager;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.lang.reflect.Type;
import java.util.List;

public class ResourcesController {

    @FXML
    private TableView<Resource> resourcesTable;

    @FXML
    private TableColumn<Resource, Integer> idColumn;

    @FXML
    private TableColumn<Resource, String> nameColumn;

    @FXML
    private TableColumn<Resource, String> typeColumn;

    @FXML
    private TableColumn<Resource, Integer> capacityColumn;

    @FXML
    private TableColumn<Resource, String> departmentColumn;

    @FXML
    private TableColumn<Resource, String> locationColumn;

    @FXML
    private TextField nameField;

    @FXML
    private TextField typeField;

    @FXML
    private TextField capacityField;

    @FXML
    private TextField departmentField;

    @FXML
    private TextField locationField;

    @FXML
    private Label errorLabel;

    private Gson gson = new Gson();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        departmentColumn.setCellValueFactory(new PropertyValueFactory<>("department"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));

        resourcesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                nameField.setText(newSelection.getName());
                typeField.setText(newSelection.getType());
                capacityField.setText(String.valueOf(newSelection.getCapacity()));
                departmentField.setText(newSelection.getDepartment());
                locationField.setText(newSelection.getLocation());
            }
        });

        loadResources();
    }

    private void loadResources() {
        Thread fetchThread = new Thread(() -> {
            try {
                ApiResponse response = ApiClient.getResources();

                if (response.getStatusCode() == 200) {
                    Type listType = new TypeToken<List<Resource>>() {}.getType();
                    List<Resource> resources = gson.fromJson(response.getBody(), listType);

                    Platform.runLater(() -> {
                        ObservableList<Resource> items = FXCollections.observableArrayList(resources);
                        resourcesTable.setItems(items);
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> errorLabel.setText("Could not load resources"));
            }
        });

        fetchThread.setDaemon(true);
        fetchThread.start();
    }

    @FXML
    private void handleAdd() {
        errorLabel.setText("");

        String name = nameField.getText();
        String type = typeField.getText();
        String department = departmentField.getText();
        String location = locationField.getText();
        int capacity;

        try {
            capacity = Integer.parseInt(capacityField.getText());
        } catch (NumberFormatException e) {
            errorLabel.setText("Capacity must be a number");
            return;
        }

        if (name.isEmpty() || type.isEmpty()) {
            errorLabel.setText("Name and type are required");
            return;
        }

        Thread addThread = new Thread(() -> {
            try {
                ApiClient.createResource(name, type, capacity, department, location);
                loadResources();
            } catch (Exception e) {
                Platform.runLater(() -> errorLabel.setText("Could not create resource"));
            }
        });

        addThread.setDaemon(true);
        addThread.start();
    }

    @FXML
    private void handleUpdate() {
        errorLabel.setText("");

        Resource selected = resourcesTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            errorLabel.setText("Select a resource to update first");
            return;
        }

        String name = nameField.getText();
        String type = typeField.getText();
        String department = departmentField.getText();
        String location = locationField.getText();
        int capacity;

        try {
            capacity = Integer.parseInt(capacityField.getText());
        } catch (NumberFormatException e) {
            errorLabel.setText("Capacity must be a number");
            return;
        }

        Thread updateThread = new Thread(() -> {
            try {
                ApiClient.updateResource(selected.getId(), name, type, capacity, department, location);
                loadResources();
            } catch (Exception e) {
                Platform.runLater(() -> errorLabel.setText("Could not update resource"));
            }
        });

        updateThread.setDaemon(true);
        updateThread.start();
    }

    @FXML
    private void handleDelete() {
        errorLabel.setText("");

        Resource selected = resourcesTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            errorLabel.setText("Select a resource to delete first");
            return;
        }

        Thread deleteThread = new Thread(() -> {
            try {
                ApiResponse response = ApiClient.deleteResource(selected.getId());

                if (response.getStatusCode() == 200) {
                    loadResources();
                } else {
                    Platform.runLater(() -> errorLabel.setText("Could not delete: resource may have existing bookings"));
                }
            } catch (Exception e) {
                Platform.runLater(() -> errorLabel.setText("Could not delete resource"));
            }
        });

        deleteThread.setDaemon(true);
        deleteThread.start();
    }

    @FXML
    private void handleGoToBookings() {
        switchScene("bookings.fxml");
    }

    @FXML
    private void handleGoToUtilization() {
        switchScene("utilization.fxml");
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