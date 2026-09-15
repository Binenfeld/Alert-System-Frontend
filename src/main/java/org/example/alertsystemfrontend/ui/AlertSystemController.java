package org.example.alertsystemfrontend.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import javafx.stage.Window;

import org.example.alertsystemfrontend.model.AlertContact;
import org.example.alertsystemfrontend.model.Company;
import org.example.alertsystemfrontend.network.BackendClient;
import org.example.alertsystemfrontend.network.SocketBackendClient;
import org.example.alertsystemfrontend.validation.Validation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

// Bound to alert-system-view.fxml (fx:controller). JavaFX creates one instance of this class
// with its no-arg constructor when the FXML is loaded, then injects every @FXML field by
// matching fx:id attributes in the FXML to field names here, and finally calls initialize().
public class AlertSystemController {

    @FXML
    private ListView<Company> companiesListView;

    @FXML
    private ListView<AlertContact> alertContactsListView;

    @FXML
    private StackPane loadingOverlay;

    private final ObservableList<Company> companies = FXCollections.observableArrayList();
    private final ObservableList<AlertContact> alertContacts = FXCollections.observableArrayList();

    // Swap this for a different BackendClient (e.g. a fake one) if you ever want to test the UI
    // without a real backend connection.
    private final BackendClient backendClient = new SocketBackendClient();

    @FXML
    public void initialize() {
        companiesListView.setItems(companies);
        companiesListView.setCellFactory(list -> new CompanyListCell());
        companiesListView.setPlaceholder(new Label("אין חברות. לחצו על \"הוסף\" כדי להוסיף חברה."));

        alertContactsListView.setItems(alertContacts);
        alertContactsListView.setCellFactory(list -> new AlertContactListCell());
        alertContactsListView.setPlaceholder(new Label("אין אנשי קשר להתראה. לחצו על \"הוסף\" כדי להוסיף."));
    }

    @FXML
    private void onAddCompany() {
        Window owner = companiesListView.getScene().getWindow();
        Dialogs.showAddCompanyDialog(owner).ifPresent(companies::add);
    }

    @FXML
    private void onAddAlertContact() {
        Window owner = alertContactsListView.getScene().getWindow();
        Dialogs.showAddAlertContactDialog(owner).ifPresent(alertContacts::add);
    }

    @FXML
    private void onRunHistoricalCheck() {
        Window owner = companiesListView.getScene().getWindow();
        Dialogs.showHistoricalCheckDialog(owner).ifPresent(company ->
                sendWithLoading(owner, () -> backendClient.sendHistoricalCheckRequest(company),
                        "בקשת הבדיקה ההיסטורית נשלחה בהצלחה לשרת.",
                        "שליחת הבקשה לשרת נכשלה. נסה שוב מאוחר יותר."));
    }

    @FXML
    private void onSubmitUpdate() {
        Window owner = companiesListView.getScene().getWindow();
        List<String> errors = validateAll();
        if (!errors.isEmpty()) {
            Dialogs.showValidationErrors(owner, errors);
            return;
        }
        sendWithLoading(owner, () -> backendClient.sendUpdate(companies, alertContacts),
                "העדכון נשלח בהצלחה לשרת.",
                "שליחת העדכון לשרת נכשלה. נסה שוב מאוחר יותר.");
    }

    private List<String> validateAll() {
        List<String> errors = new ArrayList<>();

        int index = 1;
        for (Company company : companies) {
            String name = company.nameProperty().get();
            String label = Validation.isBlank(name) ? "חברה מס' " + index : name;
            if (Validation.isBlank(name)) {
                errors.add(label + ": יש להזין שם חברה");
            }
            if (!Validation.isValidCompanyId(company.companyIdProperty().get())) {
                errors.add(label + ": מספר ח\"פ לא תקין");
            }
            if (company.peopleImpressionProperty().get() == null) {
                errors.add(label + ": חסרה התרשמות מאנשים");
            }
            if (company.financialImpressionProperty().get() == null) {
                errors.add(label + ": חסרה התרשמות פיננסית");
            }
            index++;
        }

        index = 1;
        for (AlertContact contact : alertContacts) {
            String name = contact.nameProperty().get();
            String label = Validation.isBlank(name) ? "איש קשר מס' " + index : name;
            if (Validation.isBlank(name)) {
                errors.add(label + ": יש להזין שם");
            }
            if (!Validation.isValidEmail(contact.emailProperty().get())) {
                errors.add(label + ": כתובת המייל אינה תקינה");
            }
            index++;
        }

        return errors;
    }

    private void sendWithLoading(Window owner, Callable<Boolean> action, String successMessage, String failureMessage) {
        showLoading();

        Task<Boolean> sendTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return action.call();
            }
        };
        sendTask.setOnSucceeded(e -> {
            hideLoading();
            Dialogs.showResultDialog(owner, sendTask.getValue(), successMessage, failureMessage);
        });
        sendTask.setOnFailed(e -> {
            hideLoading();
            Dialogs.showResultDialog(owner, false, successMessage, failureMessage);
        });

        Thread thread = new Thread(sendTask, "backend-request");
        thread.setDaemon(true);
        thread.start();
    }

    private void showLoading() {
        loadingOverlay.setVisible(true);
        loadingOverlay.setManaged(true);
    }

    private void hideLoading() {
        loadingOverlay.setVisible(false);
        loadingOverlay.setManaged(false);
    }
}
