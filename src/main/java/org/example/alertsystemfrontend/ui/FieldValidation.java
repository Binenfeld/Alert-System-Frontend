package org.example.alertsystemfrontend.ui;

import javafx.animation.PauseTransition;
import javafx.geometry.Point2D;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

import java.util.function.Predicate;

public final class FieldValidation {

    private FieldValidation() {
    }

    public static void attachValidator(TextField field, Predicate<String> isValid, String errorMessage) {
        field.textProperty().addListener((obs, oldValue, newValue) -> {
            if (isValid.test(newValue)) {
                field.getStyleClass().remove("field-error");
            }
        });
        field.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (isFocused) return;
            if (isValid.test(field.getText())) {
                field.getStyleClass().remove("field-error");
            } else {
                if (!field.getStyleClass().contains("field-error")) {
                    field.getStyleClass().add("field-error");
                }
                showPopup(field, errorMessage);
            }
        });
    }

    private static void showPopup(TextField field, String message) {
        if (field.getScene() == null || field.getScene().getWindow() == null) {
            return;
        }
        Tooltip tooltip = new Tooltip(message);
        tooltip.setStyle("-fx-background-color: #FFF3F3; -fx-text-fill: #B00020; "
                + "-fx-border-color: #B00020; -fx-border-width: 1; -fx-font-size: 11px; -fx-padding: 4 8;");
        Point2D point = field.localToScreen(0, field.getHeight() + 2);
        if (point == null) {
            return;
        }
        tooltip.show(field.getScene().getWindow(), point.getX(), point.getY());

        PauseTransition hideDelay = new PauseTransition(Duration.seconds(4));
        hideDelay.setOnFinished(e -> tooltip.hide());
        hideDelay.play();
    }
}
