package org.example.alertsystemfrontend.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.example.alertsystemfrontend.network.BackendClient;
import org.example.alertsystemfrontend.network.InitialData;
import org.example.alertsystemfrontend.network.SocketBackendClient;

import java.io.IOException;

public class AlertSystemApplication extends Application {

    private final BackendClient backendClient = new SocketBackendClient();
    private InitialData initialData;
    private Exception connectionError;

    // Called by the JavaFX launcher before start() and before any Stage/Scene exists, and
    // guaranteed NOT to run on the JavaFX Application Thread -- exactly "connect before any UI is
    // shown". Blocking here is intentional; the window only appears once this returns.
    @Override
    public void init() {
        try {
            initialData = backendClient.connect();
        } catch (Exception e) {
            connectionError = e;
        }
    }

    @Override
    public void start(Stage stage) throws IOException {
        if (connectionError != null) {
            Dialogs.showResultDialog(null, false, "",
                    "לא ניתן להתחבר לשרת. ודאו שהשרת פועל ונסו שוב.\n(" + connectionError.getMessage() + ")");
            Platform.exit();
            return;
        }

        FXMLLoader fxmlLoader = new FXMLLoader(AlertSystemApplication.class.getResource("alert-system-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 640, 760);

        AlertSystemController controller = fxmlLoader.getController();
        controller.setBackendClient(backendClient);
        controller.setInitialData(initialData);

        stage.setTitle("מערכת התרעה");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        if (connectionError == null) {
            backendClient.disconnect();
        }
    }
}
