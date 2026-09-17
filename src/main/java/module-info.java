module org.example.alertsystemfrontend {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;

    opens org.example.alertsystemfrontend.ui to javafx.fxml;
    opens org.example.alertsystemfrontend.model to com.google.gson;

    exports org.example.alertsystemfrontend;
    exports org.example.alertsystemfrontend.ui;
    exports org.example.alertsystemfrontend.model;
    exports org.example.alertsystemfrontend.validation;
    exports org.example.alertsystemfrontend.network;
}
