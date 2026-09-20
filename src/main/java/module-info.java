module com.smartcampus.smartcampusadmin {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.google.gson;

    opens com.smartcampus.smartcampusadmin to javafx.fxml;
    opens com.smartcampus.smartcampusadmin.controller to javafx.fxml;
    opens com.smartcampus.smartcampusadmin.model to com.google.gson;

    exports com.smartcampus.smartcampusadmin;
    exports com.smartcampus.smartcampusadmin.controller;
    exports com.smartcampus.smartcampusadmin.model;
}