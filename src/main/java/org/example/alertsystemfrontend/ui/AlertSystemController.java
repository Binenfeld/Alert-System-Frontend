package org.example.alertsystemfrontend.ui;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import org.example.alertsystemfrontend.model.AlertContact;
import org.example.alertsystemfrontend.model.ChangeTracker;
import org.example.alertsystemfrontend.model.Company;
import org.example.alertsystemfrontend.network.BackendClient;
import org.example.alertsystemfrontend.network.InitialData;
import org.example.alertsystemfrontend.validation.Validation;

import java.util.ArrayList;
import java.util.List;

// Bound to alert-system-view.fxml (fx:controller). JavaFX creates one instance of this class
// with its no-arg constructor when the FXML is loaded, then injects every @FXML field by
// matching fx:id attributes in the FXML to field names here, and finally calls initialize().
// AlertSystemApplication then calls setBackendClient()/setInitialData() once, right after loading
// the FXML and before the window is shown.
public class AlertSystemController {

    @FXML
    private VBox companiesBox;

    @FXML
    private Label companiesPlaceholder;

    @FXML
    private VBox alertContactsBox;

    @FXML
    private Label alertContactsPlaceholder;

    @FXML
    private StackPane loadingOverlay;

    private final ObservableList<Company> companies = FXCollections.observableArrayList();
    private final ObservableList<AlertContact> alertContacts = FXCollections.observableArrayList();

    // True from the moment a company/contact is added, removed, or edited, until the next
    // successful send of that list (see onSubmitUpdate, which only calls markClean() on the
    // tracker(s) whose send actually succeeded).
    public final ChangeTracker<Company> companiesChangeTracker = new ChangeTracker<>(companies, company ->
            new Observable[]{
                    company.nameProperty(), company.companyIdProperty(),
                    company.peopleImpressionProperty(), company.financialImpressionProperty(),
                    company.unpaidMonthsProperty(), company.getComments()
            });
    public final ChangeTracker<AlertContact> alertContactsChangeTracker = new ChangeTracker<>(alertContacts,
            contact -> new Observable[]{contact.nameProperty(), contact.emailProperty()});

    // Set via setBackendClient() by AlertSystemApplication, which owns the single persistent
    // connection for the whole app session.
    private BackendClient backendClient;

    @FXML
    public void initialize() {
        companies.addListener((ListChangeListener<Company>) change -> refreshCompaniesBox());
        alertContacts.addListener((ListChangeListener<AlertContact>) change -> refreshAlertContactsBox());
        refreshCompaniesBox();
        refreshAlertContactsBox();
    }

    private void refreshCompaniesBox() {
        companiesBox.getChildren().clear();
        for (Company company : companies) {
            companiesBox.getChildren().add(
                    CompanyRowFactory.buildRow(company, companies, () -> companiesBox.getScene().getWindow()));
        }
        companiesPlaceholder.setVisible(companies.isEmpty());
        companiesPlaceholder.setManaged(companies.isEmpty());
    }

    private void refreshAlertContactsBox() {
        alertContactsBox.getChildren().clear();
        for (AlertContact contact : alertContacts) {
            alertContactsBox.getChildren().add(AlertContactRowFactory.buildRow(contact, alertContacts));
        }
        alertContactsPlaceholder.setVisible(alertContacts.isEmpty());
        alertContactsPlaceholder.setManaged(alertContacts.isEmpty());
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
        Window owner = companiesBox.getScene().getWindow();
        Dialogs.showAddCompanyDialog(owner).ifPresent(companies::add);
    }

    @FXML
    private void onAddAlertContact() {
        Window owner = alertContactsBox.getScene().getWindow();
        Dialogs.showAddAlertContactDialog(owner).ifPresent(alertContacts::add);
    }

    @FXML
    private void onRunHistoricalCheck() {
        Window owner = companiesBox.getScene().getWindow();
        Dialogs.showHistoricalCheckDialog(owner).ifPresent(company ->
                sendFireAndForget(() -> backendClient.sendHistoricalCheckRequest(company)));
    }

    // Tells the backend what changed and moves on -- it never waits for or shows any outcome from
    // the backend (the alert contacts are the ones told whether it succeeded, by email, from the
    // backend side). A list's tracker is only marked clean once its send didn't throw, so a failed
    // send is retried the next time the user presses submit.
    @FXML
    private void onSubmitUpdate() {
        Window owner = companiesBox.getScene().getWindow();
        List<String> errors = validateAll();
        if (!errors.isEmpty()) {
            Dialogs.showValidationErrors(owner, errors);
            return;
        }

        boolean shouldSendCompanies = companiesChangeTracker.isChanged();
        boolean shouldSendContacts = alertContactsChangeTracker.isChanged();

        if (!shouldSendCompanies && !shouldSendContacts) {
            return;
        }

        showLoading();

        Task<SendOutcome> sendTask = new Task<>() {
            @Override
            protected SendOutcome call() {
                boolean companiesOk = true;
                boolean contactsOk = true;
                if (shouldSendCompanies) {
                    try {
                        backendClient.sendCompaniesUpdate(companies);
                    } catch (Exception e) {
                        companiesOk = false;
                    }
                }
                if (shouldSendContacts) {
                    try {
                        backendClient.sendContactsUpdate(alertContacts);
                    } catch (Exception e) {
                        contactsOk = false;
                    }
                }
                return new SendOutcome(shouldSendCompanies, companiesOk, shouldSendContacts, contactsOk);
            }
        };
        sendTask.setOnSucceeded(e -> {
            hideLoading();
            SendOutcome outcome = sendTask.getValue();
            if (outcome.companiesAttempted() && outcome.companiesOk()) {
                companiesChangeTracker.markClean();
            }
            if (outcome.contactsAttempted() && outcome.contactsOk()) {
                alertContactsChangeTracker.markClean();
            }
        });
        sendTask.setOnFailed(e -> hideLoading());

        Thread thread = new Thread(sendTask, "submit-update-request");
        thread.setDaemon(true);
        thread.start();
    }

    private record SendOutcome(boolean companiesAttempted, boolean companiesOk,
                                boolean contactsAttempted, boolean contactsOk) {
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

    // Runs a backend call on a background thread with a loading indicator, but never shows any
    // outcome from it -- a failure is only logged, since nothing about what the backend did with
    // the request is the frontend's to display (see BackendClient).
    private void sendFireAndForget(BackendAction action) {
        showLoading();

        Task<Void> sendTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                action.run();
                return null;
            }
        };
        sendTask.setOnSucceeded(e -> hideLoading());
        sendTask.setOnFailed(e -> {
            hideLoading();
            System.out.println("backend request failed: " + sendTask.getException());
        });

        Thread thread = new Thread(sendTask, "backend-request");
        thread.setDaemon(true);
        thread.start();
    }

    @FunctionalInterface
    private interface BackendAction {
        void run() throws Exception;
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
