package org.example.alertsystemfrontend.ui;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import org.example.alertsystemfrontend.model.Company;
import org.example.alertsystemfrontend.validation.Validation;

import java.util.function.Supplier;

// Builds one row for a Company. Plain factory, not a ListCell -- the companies list is a simple
// VBox (see AlertSystemController), not a virtualized ListView, since these rows never needed
// selection and a small VBox has none of ListView's virtualization quirks.
public final class CompanyRowFactory {

    private CompanyRowFactory() {
    }

    public static HBox buildRow(Company company, ObservableList<Company> companies, Supplier<Window> ownerSupplier) {
        HBox row = new HBox(10);
        row.getStyleClass().add("row");
        row.setAlignment(Pos.CENTER_LEFT);

        Button deleteBtn = new Button("מחק");
        deleteBtn.getStyleClass().addAll("btn-secondary", "small");
        deleteBtn.setOnAction(e -> companies.remove(company));

        Button commentsBtn = new Button("הערות");
        commentsBtn.getStyleClass().addAll("btn-secondary", "small");
        commentsBtn.setOnAction(e -> Dialogs.showCompanyCommentsDialog(ownerSupplier.get(), company));

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

    private static HBox createUnpaidMonthsRow(Company company) {
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
