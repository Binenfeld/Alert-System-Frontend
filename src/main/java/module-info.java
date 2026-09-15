module org.example.alertsystemfrontend {
    requires javafx.controls;
    requires javafx.fxml;

    opens org.example.alertsystemfrontend.ui to javafx.fxml;

    exports org.example.alertsystemfrontend;
    exports org.example.alertsystemfrontend.ui;
    exports org.example.alertsystemfrontend.model;
    exports org.example.alertsystemfrontend.validation;
    exports org.example.alertsystemfrontend.network;
}
