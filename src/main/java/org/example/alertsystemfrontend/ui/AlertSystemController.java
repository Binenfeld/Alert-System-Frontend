package org.example.alertsystemfrontend.ui;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import javafx.stage.Window;

import org.example.alertsystemfrontend.model.AlertContact;
import org.example.alertsystemfrontend.model.ChangeTracker;
import org.example.alertsystemfrontend.model.Company;
import org.example.alertsystemfrontend.network.BackendClient;
import org.example.alertsystemfrontend.network.InitialData;
import org.example.alertsystemfrontend.validation.Validation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

// Bound to alert-system-view.fxml (fx:controller). JavaFX creates one instance of this class
// with its no-arg constructor when the FXML is loaded, then injects every @FXML field by
// matching fx:id attributes in the FXML to field names here, and finally calls initialize().
// AlertSystemApplication then calls setBackendClient()/setInitialData() once, right after loading
// the FXML and before the window is shown.
public class AlertSystemController {

    @FXML
    private ListView<Company> companiesListView;

    @FXML
    private ListView<AlertContact> alertContactsListView;

    @FXML
    private StackPane loadingOverlay;

    private final ObservableList<Company> companies = FXCollections.observableArrayList();
    private final ObservableList<AlertContact> alertContacts = FXCollections.observableArrayList();

    // True from the moment a company/contact is added, removed, or edited, until the next
    // successful send of that list (see onSubmitUpdate, which only calls markClean() on the
    // tracker(s) whose send actually succeeded).
    private final ChangeTracker<Company> companiesChangeTracker = new ChangeTracker<>(companies, company ->
            new Observable[]{
                    company.nameProperty(), company.companyIdProperty(),
                    company.peopleImpressionProperty(), company.financialImpressionProperty(),
                    company.unpaidMonthsProperty(), company.getComments()
            });
    private final ChangeTracker<AlertContact> alertContactsChangeTracker = new ChangeTracker<>(alertContacts,
            contact -> new Observable[]{contact.nameProperty(), contact.emailProperty()});

    // Set via setBackendClient() by AlertSystemApplication, which owns the single persistent
    // connection for the whole app session.
    private BackendClient backendClient;

    @FXML
    public void initialize() {
        companiesListView.setItems(companies);
        companiesListView.setCellFactory(list -> new CompanyListCell());
        companiesListView.setPlaceholder(new Label("אין חברות. לחצו על \"הוסף\" כדי להוסיף חברה."));

        alertContactsListView.setItems(alertContacts);
        alertContactsListView.setCellFactory(list -> new AlertContactListCell());
        alertContactsListView.setPlaceholder(new Label("אין אנשי קשר להתראה. לחצו על \"הוסף\" כדי להוסיף."));
    }

    public void setBackendClient(BackendClient backendClient) {
        this.backendClient = backendClient;
    }

    // Populates the lists from the server's initial push. This is baseline data from the server,
    // not a local edit, so both trackers are marked clean right after.
    public void setInitialData(InitialData data) {
        companies.setAll(data.companies());
        alertContacts.setAll(data.alertContacts());
        companiesChangeTracker.markClean();
        alertContactsChangeTracker.markClean();
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

        boolean shouldSendCompanies = companiesChangeTracker.isChanged();
        boolean shouldSendContacts = alertContactsChangeTracker.isChanged();

        if (!shouldSendCompanies && !shouldSendContacts) {
            Dialogs.showResultDialog(owner, true, "אין שינויים חדשים לשליחה.", "");
            return;
        }

        showLoading();

        Task<UpdateResult> sendTask = new Task<>() {
            @Override
            protected UpdateResult call() {
                boolean companiesOk = true;
                boolean contactsOk = true;
                if (shouldSendCompanies) {
                    try {
                        companiesOk = backendClient.sendCompaniesUpdate(companies);
                    } catch (Exception e) {
                        companiesOk = false;
                    }
                }
                if (shouldSendContacts) {
                    try {
                        contactsOk = backendClient.sendContactsUpdate(alertContacts);
                    } catch (Exception e) {
                        contactsOk = false;
                    }
                }
                return new UpdateResult(shouldSendCompanies, companiesOk, shouldSendContacts, contactsOk);
            }
        };
        sendTask.setOnSucceeded(e -> {
            hideLoading();
            UpdateResult result = sendTask.getValue();
            if (result.companiesAttempted() && result.companiesSucceeded()) {
                companiesChangeTracker.markClean();
            }
            if (result.contactsAttempted() && result.contactsSucceeded()) {
                alertContactsChangeTracker.markClean();
            }
            String message = buildUpdateResultMessage(result);
            Dialogs.showResultDialog(owner, result.isFullSuccess(), message, message);
        });
        sendTask.setOnFailed(e -> {
            hideLoading();
            String message = "שליחת העדכון לשרת נכשלה. נסה שוב מאוחר יותר.";
            Dialogs.showResultDialog(owner, false, message, message);
        });

        Thread thread = new Thread(sendTask, "submit-update-request");
        thread.setDaemon(true);
        thread.start();
    }

    private String buildUpdateResultMessage(UpdateResult result) {
        List<String> parts = new ArrayList<>();
        if (result.companiesAttempted()) {
            parts.add("חברות: " + (result.companiesSucceeded() ? "נשלחו בהצלחה" : "השליחה נכשלה"));
        }
        if (result.contactsAttempted()) {
            parts.add("אנשי קשר להתראה: " + (result.contactsSucceeded() ? "נשלחו בהצלחה" : "השליחה נכשלה"));
        }
        return String.join("\n", parts);
    }

    private record UpdateResult(boolean companiesAttempted, boolean companiesSucceeded,
                                 boolean contactsAttempted, boolean contactsSucceeded) {
        boolean isFullSuccess() {
            return (!companiesAttempted || companiesSucceeded) && (!contactsAttempted || contactsSucceeded);
        }
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
