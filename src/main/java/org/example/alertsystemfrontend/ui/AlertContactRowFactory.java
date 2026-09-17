package org.example.alertsystemfrontend.ui;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import org.example.alertsystemfrontend.model.AlertContact;
import org.example.alertsystemfrontend.validation.Validation;

// Builds one row for an AlertContact. Plain factory, not a ListCell -- see CompanyRowFactory for
// why this list is a simple VBox rather than a ListView.
public final class AlertContactRowFactory {

    private AlertContactRowFactory() {
    }

    public static HBox buildRow(AlertContact contact, ObservableList<AlertContact> alertContacts) {
        HBox row = new HBox(10);
        row.getStyleClass().add("row");
        row.setAlignment(Pos.CENTER_LEFT);

        Button deleteBtn = new Button("מחק");
        deleteBtn.getStyleClass().addAll("btn-secondary", "small");
        deleteBtn.setOnAction(e -> alertContacts.remove(contact));

        VBox fields = new VBox(6);
        HBox.setHgrow(fields, Priority.ALWAYS);
        fields.getChildren().addAll(
                FormFields.labeledField("שם:", "הזן שם", contact.nameProperty(),
                        v -> !Validation.isBlank(v), "יש להזין שם"),
                FormFields.labeledField("מייל:", "הזן כתובת מייל", contact.emailProperty(),
                        Validation::isValidEmail, "כתובת המייל אינה תקינה")
        );

        row.getChildren().addAll(deleteBtn, fields);
        return row;
    }
}
