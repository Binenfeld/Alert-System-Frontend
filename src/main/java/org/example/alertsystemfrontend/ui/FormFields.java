package org.example.alertsystemfrontend.ui;

import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import org.example.alertsystemfrontend.model.ImpressionLevels;

import java.util.function.Predicate;

// Shared "label: field" row builders used by the list cells and the dialogs, so the same
// look and validation wiring doesn't get re-implemented in three different places.
public final class FormFields {

    private FormFields() {
    }

    public static HBox labeledField(String labelText, String prompt, StringProperty value) {
        return labeledField(labelText, buildTextField(prompt, value));
    }

    public static HBox labeledField(String labelText, String prompt, StringProperty value,
                                     Predicate<String> validator, String errorMessage) {
        TextField field = buildTextField(prompt, value);
        FieldValidation.attachValidator(field, validator, errorMessage);
        return labeledField(labelText, field);
    }

    public static HBox labeledField(String labelText, TextField field) {
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER_RIGHT);
        Label label = new Label(labelText);
        label.getStyleClass().add("field-label");
        HBox.setHgrow(field, Priority.ALWAYS);
        box.getChildren().addAll(label, field);
        return box;
    }

    public static HBox labeledCombo(String labelText, StringProperty value) {
        ComboBox<String> combo = impressionCombo();
        combo.valueProperty().bindBidirectional(value);
        return labeledCombo(labelText, combo);
    }

    public static HBox labeledCombo(String labelText, ComboBox<String> combo) {
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER_RIGHT);
        Label label = new Label(labelText);
        label.getStyleClass().add("field-label");
        HBox.setHgrow(combo, Priority.ALWAYS);
        box.getChildren().addAll(label, combo);
        return box;
    }

    public static ComboBox<String> impressionCombo() {
        ComboBox<String> combo = new ComboBox<>(ImpressionLevels.OPTIONS);
        combo.getStyleClass().add("impression-combo");
        combo.setMaxWidth(Double.MAX_VALUE);
        return combo;
    }

    private static TextField buildTextField(String prompt, StringProperty value) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.getStyleClass().add("text-field");
        field.textProperty().bindBidirectional(value);
        return field;
    }
}
