package org.example.alertsystemfrontend.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class ImpressionLevels {

    public static final ObservableList<String> OPTIONS =
            FXCollections.unmodifiableObservableList(
                    FXCollections.observableArrayList("מצוין", "טוב", "בינוני", "רע"));

    private ImpressionLevels() {
    }
}
