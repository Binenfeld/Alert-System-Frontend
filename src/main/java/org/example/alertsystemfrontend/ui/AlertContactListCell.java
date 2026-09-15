package org.example.alertsystemfrontend.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import org.example.alertsystemfrontend.model.AlertContact;
import org.example.alertsystemfrontend.validation.Validation;

public class AlertContactListCell extends ListCell<AlertContact> {

    @Override
    protected void updateItem(AlertContact contact, boolean empty) {
        super.updateItem(contact, empty);
        setText(null);
        setGraphic(empty || contact == null ? null : buildRow(contact));
    }

    private HBox buildRow(AlertContact contact) {
        HBox row = new HBox(10);
        row.getStyleClass().add("row");
        row.setAlignment(Pos.CENTER_LEFT);

        Button deleteBtn = new Button("מחק");
        deleteBtn.getStyleClass().addAll("btn-secondary", "small");
        deleteBtn.setOnAction(e -> getListView().getItems().remove(contact));

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
