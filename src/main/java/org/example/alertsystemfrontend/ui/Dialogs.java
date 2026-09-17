package org.example.alertsystemfrontend.ui;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.example.alertsystemfrontend.model.AlertContact;
import org.example.alertsystemfrontend.model.Comment;
import org.example.alertsystemfrontend.model.Company;
import org.example.alertsystemfrontend.model.ImpressionLevel;
import org.example.alertsystemfrontend.validation.Validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public final class Dialogs {

    private static final ButtonType CANCEL_TYPE = new ButtonType("ביטול", ButtonBar.ButtonData.CANCEL_CLOSE);

    private Dialogs() {
    }

    public static Optional<Company> showAddCompanyDialog(Window owner) {
        Dialog<Company> dialog = new Dialog<>();
        setupDialog(dialog, owner, "חברה חדשה");

        CompanyFieldsForm form = new CompanyFieldsForm();
        dialog.getDialogPane().setContent(form.content);

        ButtonType addType = new ButtonType("הוסף", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addType, CANCEL_TYPE);
        styleAsPrimary(dialog, addType);
        styleAsSecondary(dialog, CANCEL_TYPE);
        attachSubmitValidation(dialog, addType, form::validate, form.errorLabel);

        dialog.setResultConverter(button -> button == addType ? form.toCompany() : null);

        return dialog.showAndWait();
    }

    public static Optional<Company> showHistoricalCheckDialog(Window owner) {
        Dialog<Company> dialog = new Dialog<>();
        setupDialog(dialog, owner, "בדיקה היסטורית");

        CompanyFieldsForm form = new CompanyFieldsForm();
        dialog.getDialogPane().setContent(form.content);

        ButtonType sendType = new ButtonType("שלח", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(sendType, CANCEL_TYPE);
        styleAsPrimary(dialog, sendType);
        styleAsSecondary(dialog, CANCEL_TYPE);
        attachSubmitValidation(dialog, sendType, form::validate, form.errorLabel);

        dialog.setResultConverter(button -> button == sendType ? form.toCompany() : null);

        return dialog.showAndWait();
    }

    public static Optional<AlertContact> showAddAlertContactDialog(Window owner) {
        Dialog<AlertContact> dialog = new Dialog<>();
        setupDialog(dialog, owner, "איש קשר חדש להתראה");

        TextField nameField = new TextField();
        nameField.setPromptText("הזן שם");
        nameField.getStyleClass().add("text-field");
        TextField emailField = new TextField();
        emailField.setPromptText("הזן כתובת מייל");
        emailField.getStyleClass().add("text-field");
        FieldValidation.attachValidator(nameField, v -> !Validation.isBlank(v), "יש להזין שם");
        FieldValidation.attachValidator(emailField, Validation::isValidEmail, "כתובת המייל אינה תקינה");

        Label errorLabel = createErrorLabel();

        VBox content = new VBox(12, errorLabel,
                FormFields.labeledField("שם:", nameField), FormFields.labeledField("מייל:", emailField));
        content.setPadding(new Insets(16));
        dialog.getDialogPane().setContent(content);

        ButtonType addType = new ButtonType("הוסף", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addType, CANCEL_TYPE);
        styleAsPrimary(dialog, addType);
        styleAsSecondary(dialog, CANCEL_TYPE);

        Supplier<List<String>> validate = () -> {
            List<String> errors = new ArrayList<>();
            if (Validation.isBlank(nameField.getText())) {
                errors.add("יש להזין שם");
            }
            if (!Validation.isValidEmail(emailField.getText())) {
                errors.add("כתובת המייל אינה תקינה");
            }
            return errors;
        };
        attachSubmitValidation(dialog, addType, validate, errorLabel);

        dialog.setResultConverter(button -> {
            if (button != addType) return null;
            AlertContact contact = new AlertContact();
            contact.nameProperty().set(nameField.getText());
            contact.emailProperty().set(emailField.getText());
            return contact;
        });

        return dialog.showAndWait();
    }

    public static void showCompanyCommentsDialog(Window owner, Company company) {
        Dialog<Void> dialog = new Dialog<>();
        String companyName = company.nameProperty().get();
        setupDialog(dialog, owner, companyName == null || companyName.isBlank() ? "הערות" : "הערות - " + companyName);

        ListView<Comment> commentsListView = new ListView<>(company.getComments());
        commentsListView.getStyleClass().add("entry-list");
        commentsListView.setPrefSize(340, 220);
        commentsListView.setPlaceholder(new Label("אין הערות"));
        commentsListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Comment item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setWrapText(true);
                    setText("[" + item.getTimestamp() + "] " + item.getText());
                }
            }
        });

        Label errorLabel = createErrorLabel();

        TextArea newCommentArea = new TextArea();
        newCommentArea.setPromptText("כתוב הערה חדשה...");
        newCommentArea.setPrefRowCount(3);
        newCommentArea.setWrapText(true);
        newCommentArea.getStyleClass().add("comment-input");

        Button addCommentBtn = new Button("הוסף הערה");
        addCommentBtn.getStyleClass().add("btn-primary");
        addCommentBtn.setOnAction(e -> {
            String text = newCommentArea.getText().trim();
            if (text.isEmpty()) {
                showErrors(errorLabel, List.of("לא ניתן להוסיף הערה ריקה"));
                resizeToContent(dialog);
            } else {
                hideErrors(errorLabel);
                company.addComment(text);
                newCommentArea.clear();
            }
        });

        HBox addRow = new HBox(addCommentBtn);
        addRow.setAlignment(Pos.CENTER_RIGHT);

        VBox content = new VBox(10, errorLabel, commentsListView, newCommentArea, addRow);
        content.setPadding(new Insets(16));
        dialog.getDialogPane().setContent(content);

        ButtonType closeType = new ButtonType("סגור", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeType);
        styleAsSecondary(dialog, closeType);

        dialog.showAndWait();
    }

    public static void showValidationErrors(Window owner, List<String> errors) {
        Dialog<Void> dialog = new Dialog<>();
        setupDialog(dialog, owner, "יש לתקן שגיאות לפני השליחה");

        Label label = new Label(String.join("\n", errors));
        label.setWrapText(true);
        label.getStyleClass().add("form-error");

        VBox content = new VBox(label);
        content.setPadding(new Insets(16));
        content.setPrefWidth(320);
        dialog.getDialogPane().setContent(content);

        ButtonType okType = new ButtonType("אישור", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(okType);
        styleAsPrimary(dialog, okType);

        dialog.showAndWait();
    }

    public static void showResultDialog(Window owner, boolean success, String successMessage, String failureMessage) {
        Dialog<Void> dialog = new Dialog<>();
        setupDialog(dialog, owner, success ? "הצלחה" : "שגיאה");

        Label label = new Label(success ? successMessage : failureMessage);
        label.setWrapText(true);
        label.getStyleClass().add(success ? "result-success" : "result-failure");

        VBox content = new VBox(label);
        content.setPadding(new Insets(16));
        content.setPrefWidth(280);
        dialog.getDialogPane().setContent(content);

        ButtonType okType = new ButtonType("אישור", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(okType);
        styleAsPrimary(dialog, okType);

        dialog.showAndWait();
    }

    private static void attachSubmitValidation(Dialog<?> dialog, ButtonType type,
                                                Supplier<List<String>> validator, Label errorLabel) {
        Button button = (Button) dialog.getDialogPane().lookupButton(type);
        button.addEventFilter(ActionEvent.ACTION, event -> {
            List<String> errors = validator.get();
            if (!errors.isEmpty()) {
                showErrors(errorLabel, errors);
                resizeToContent(dialog);
                event.consume();
            }
        });
    }

    private static void resizeToContent(Dialog<?> dialog) {
        Window window = dialog.getDialogPane().getScene().getWindow();
        if (window instanceof Stage stage) {
            stage.sizeToScene();
        }
    }

    private static Label createErrorLabel() {
        Label label = new Label();
        label.getStyleClass().add("form-error");
        label.setWrapText(true);
        label.setVisible(false);
        label.setManaged(false);
        return label;
    }

    private static void showErrors(Label errorLabel, List<String> errors) {
        errorLabel.setText(String.join("\n", errors));
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private static void hideErrors(Label errorLabel) {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private static void setupDialog(Dialog<?> dialog, Window owner, String title) {
        dialog.initOwner(owner);
        dialog.setTitle(title);
        dialog.getDialogPane().setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        dialog.getDialogPane().getStylesheets().add(
                Dialogs.class.getResource("alert-system-view.css").toExternalForm());
    }

    private static void styleAsPrimary(Dialog<?> dialog, ButtonType type) {
        Button button = (Button) dialog.getDialogPane().lookupButton(type);
        button.getStyleClass().add("btn-primary");
    }

    private static void styleAsSecondary(Dialog<?> dialog, ButtonType type) {
        Button button = (Button) dialog.getDialogPane().lookupButton(type);
        button.getStyleClass().add("btn-secondary");
    }

    private static final class CompanyFieldsForm {
        private final TextField nameField = new TextField();
        private final TextField companyIdField = new TextField();
        private final ComboBox<ImpressionLevel> peopleImpressionCombo = FormFields.impressionCombo();
        private final ComboBox<ImpressionLevel> financialImpressionCombo = FormFields.impressionCombo();
        private final Label errorLabel = createErrorLabel();
        private final VBox content;

        private CompanyFieldsForm() {
            nameField.setPromptText("הזן שם חברה");
            nameField.getStyleClass().add("text-field");
            companyIdField.setPromptText("הזן מספר ח\"פ");
            companyIdField.getStyleClass().add("text-field");

            FieldValidation.attachValidator(nameField, v -> !Validation.isBlank(v), "יש להזין שם חברה");
            FieldValidation.attachValidator(companyIdField, Validation::isValidCompanyId,
                    "מספר ח\"פ לא תקין (9 ספרות עם ספרת ביקורת תקינה)");

            content = new VBox(12,
                    errorLabel,
                    FormFields.labeledField("שם:", nameField),
                    FormFields.labeledField("ח\"פ:", companyIdField),
                    FormFields.labeledCombo("התרשמות מאנשים:", peopleImpressionCombo),
                    FormFields.labeledCombo("התרשמות פיננסית:", financialImpressionCombo));
            content.setPadding(new Insets(16));
        }

        private List<String> validate() {
            List<String> errors = new ArrayList<>();
            if (Validation.isBlank(nameField.getText())) {
                errors.add("יש להזין שם חברה");
            }
            if (!Validation.isValidCompanyId(companyIdField.getText())) {
                errors.add("מספר ח\"פ לא תקין (9 ספרות עם ספרת ביקורת תקינה)");
            }
            if (peopleImpressionCombo.getValue() == null) {
                errors.add("יש לבחור התרשמות מאנשים");
            }
            if (financialImpressionCombo.getValue() == null) {
                errors.add("יש לבחור התרשמות פיננסית");
            }
            return errors;
        }

        private Company toCompany() {
            Company company = new Company();
            company.nameProperty().set(nameField.getText());
            company.companyIdProperty().set(companyIdField.getText());
            company.peopleImpressionProperty().set(peopleImpressionCombo.getValue());
            company.financialImpressionProperty().set(financialImpressionCombo.getValue());
            return company;
        }
    }
}
