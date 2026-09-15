package org.example.alertsystemfrontend.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class AlertSystemApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(AlertSystemApplication.class.getResource("alert-system-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 480, 760);
        stage.setTitle("מערכת התרעה");
        stage.setScene(scene);
        stage.show();
    }
}
