module org.example.alertsystemfrontend {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.alertsystemfrontend to javafx.fxml;
    exports org.example.alertsystemfrontend;
}