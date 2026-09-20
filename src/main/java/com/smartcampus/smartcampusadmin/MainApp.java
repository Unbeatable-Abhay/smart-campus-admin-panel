package com.smartcampus.smartcampusadmin;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class MainApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        primaryStage.setTitle("Smart Campus Admin");

        switchScene("login.fxml");
        primaryStage.show();
    }

    public static void switchScene(String fxmlFileName) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource(fxmlFileName));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(MainApp.class.getResource("styles.css").toExternalForm());
        primaryStage.setScene(scene);
    }
}