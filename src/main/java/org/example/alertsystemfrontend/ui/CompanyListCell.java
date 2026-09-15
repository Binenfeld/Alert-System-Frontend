package org.example.alertsystemfrontend.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import org.example.alertsystemfrontend.model.Company;
import org.example.alertsystemfrontend.validation.Validation;

public class CompanyListCell extends ListCell<Company> {

    @Override
    protected void updateItem(Company company, boolean empty) {
        super.updateItem(company, empty);
        setText(null);
        setGraphic(empty || company == null ? null : buildRow(company));
    }

    private HBox buildRow(Company company) {
        HBox row = new HBox(10);
        row.getStyleClass().add("row");
        row.setAlignment(Pos.CENTER_LEFT);

        Button deleteBtn = new Button("מחק");
        deleteBtn.getStyleClass().addAll("btn-secondary", "small");
        deleteBtn.setOnAction(e -> getListView().getItems().remove(company));

        Button commentsBtn = new Button("הערות");
        commentsBtn.getStyleClass().addAll("btn-secondary", "small");
        commentsBtn.setOnAction(e -> Dialogs.showCompanyCommentsDialog(getListView().getScene().getWindow(), company));

        VBox buttons = new VBox(6, deleteBtn, commentsBtn);

        VBox fields = new VBox(6);
        HBox.setHgrow(fields, Priority.ALWAYS);
        fields.getChildren().addAll(
                FormFields.labeledField("שם:", "הזן שם חברה", company.nameProperty(),
                        v -> !Validation.isBlank(v), "יש להזין שם חברה"),
                FormFields.labeledField("ח\"פ:", "הזן מספר ח\"פ", company.companyIdProperty(),
                        Validation::isValidCompanyId, "מספר ח\"פ לא תקין (9 ספרות עם ספרת ביקורת תקינה)"),
                FormFields.labeledCombo("התרשמות מאנשים:", company.peopleImpressionProperty()),
                FormFields.labeledCombo("התרשמות פיננסית:", company.financialImpressionProperty()),
                createUnpaidMonthsRow(company)
        );

        row.getChildren().addAll(buttons, fields);
        return row;
    }

    private HBox createUnpaidMonthsRow(Company company) {
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER_RIGHT);

        Label label = new Label("חודשים שלא שולמו:");
        label.getStyleClass().add("field-label");

        Button minusBtn = new Button("-");
        minusBtn.getStyleClass().addAll("btn-secondary", "small", "stepper-btn");
        minusBtn.setOnAction(e -> company.decrementUnpaidMonths());

        Label countLabel = new Label();
        countLabel.getStyleClass().add("unpaid-count");
        countLabel.textProperty().bind(company.unpaidMonthsProperty().asString());

        Button plusBtn = new Button("+");
        plusBtn.getStyleClass().addAll("btn-secondary", "small", "stepper-btn");
        plusBtn.setOnAction(e -> company.incrementUnpaidMonths());

        box.getChildren().addAll(label, minusBtn, countLabel, plusBtn);
        return box;
    }
}
