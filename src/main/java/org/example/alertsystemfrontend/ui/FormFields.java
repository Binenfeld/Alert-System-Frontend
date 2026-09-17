package org.example.alertsystemfrontend.ui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import org.example.alertsystemfrontend.model.ImpressionLevel;

import java.util.EnumMap;
import java.util.Map;
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

    public static HBox labeledCombo(String labelText, ObjectProperty<ImpressionLevel> value) {
        ComboBox<ImpressionLevel> combo = impressionCombo();
        combo.valueProperty().bindBidirectional(value);
        return labeledCombo(labelText, combo);
    }

    public static HBox labeledCombo(String labelText, ComboBox<ImpressionLevel> combo) {
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER_RIGHT);
        Label label = new Label(labelText);
        label.getStyleClass().add("field-label");
        HBox.setHgrow(combo, Priority.ALWAYS);
        box.getChildren().addAll(label, combo);
        return box;
    }

    // Stoplight coloring for the impression combo: greener the better the impression, redder the
    // worse -- one CSS class per level (see .impression-combo .list-cell.impression-cell-* in
    // alert-system-view.css), applied to both the popup's list cells and the button cell (the
    // control's own always-visible "selected value" area) so the color shows whether the dropdown
    // is open or not.
    private static final Map<ImpressionLevel, String> IMPRESSION_STYLE_CLASSES = new EnumMap<>(ImpressionLevel.class);

    static {
        IMPRESSION_STYLE_CLASSES.put(ImpressionLevel.GREAT, "impression-cell-great");
        IMPRESSION_STYLE_CLASSES.put(ImpressionLevel.GOOD, "impression-cell-good");
        IMPRESSION_STYLE_CLASSES.put(ImpressionLevel.OKAY, "impression-cell-okay");
        IMPRESSION_STYLE_CLASSES.put(ImpressionLevel.BAD, "impression-cell-bad");
    }

    public static ComboBox<ImpressionLevel> impressionCombo() {
        ComboBox<ImpressionLevel> combo = new ComboBox<>(FXCollections.observableArrayList(ImpressionLevel.values()));
        combo.getStyleClass().add("impression-combo");
        combo.setMaxWidth(Double.MAX_VALUE);
        // HBox.setHgrow only controls how *extra* space is handed out -- it does nothing to let a
        // child shrink below its own default min width. Without this, a row's true required width
        // (labels + buttons + this control's default min width) can exceed the card's available
        // width without JavaFX showing it until a later relayout (e.g. the one triggered by
        // clicking the row to select it) finally reveals a horizontal scrollbar.
        combo.setMinWidth(60);
        combo.setCellFactory(list -> new ImpressionListCell());
        combo.setButtonCell(new ImpressionListCell());
        return combo;
    }

    private static final class ImpressionListCell extends ListCell<ImpressionLevel> {
        @Override
        protected void updateItem(ImpressionLevel item, boolean empty) {
            super.updateItem(item, empty);
            getStyleClass().removeAll(IMPRESSION_STYLE_CLASSES.values());
            if (empty || item == null) {
                setText(null);
            } else {
                setText(item.toString());
                getStyleClass().add(IMPRESSION_STYLE_CLASSES.get(item));
            }
        }
    }

    private static TextField buildTextField(String prompt, StringProperty value) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.getStyleClass().add("text-field");
        field.textProperty().bindBidirectional(value);
        field.setMinWidth(60);
        return field;
    }
}
