package org.example.alertsystemfrontend.network;

import org.example.alertsystemfrontend.model.AlertContact;
import org.example.alertsystemfrontend.model.Company;

import java.util.List;

// Both methods below are called by AlertSystemController (see AlertSystemController.java in the
// ui package) already running on a background thread (via javafx.concurrent.Task), so blocking
// socket I/O here is safe and will NOT freeze the window. Both are currently stubbed with a fake
// delay and a random result so the rest of the app (loading overlay, success/failure dialogs) is
// fully testable before the real backend exists.
public class SocketBackendClient implements BackendClient {

    // TODO: called when the user fills in the "בדיקה היסטורית" dialog (Dialogs.showHistoricalCheckDialog)
    //  and presses "שלח". Replace this body with the real client socket call:
    //   1. Open a java.net.Socket to the backend's host/port.
    //   2. Write a request built from `company`'s fields: company.nameProperty().get(),
    //      company.companyIdProperty().get(), company.peopleImpressionProperty().get(),
    //      company.financialImpressionProperty().get() -- in whatever wire format/protocol
    //      the backend expects (e.g. a line of text, JSON, a custom binary format).
    //   3. Read the backend's response and return true only if it confirms success.
    //   4. Close the socket before returning -- remember only one client may be connected to the
    //      backend at a time, so a leaked/open connection here would block every other action.
    @Override
    public boolean sendHistoricalCheckRequest(Company company) throws Exception {
        Thread.sleep(1500);
        return Math.random() > 0.2;
    }

    // TODO: called when the user presses "שלח עדכון", after AlertSystemController.validateAll()
    //  has already confirmed every company and every alert contact is fully filled in and valid,
    //  so you can trust the data here needs no further checking. Replace this body with the real
    //  client socket call: open a Socket, serialize `companies` and/or `alertContacts` (including
    //  each Company's comments via company.getComments() and unpaid-months count via
    //  company.unpaidMonthsProperty().get()), send it, read back the confirmation, close the
    //  socket, and return true only on success.
    //  `companiesChanged`/`alertContactsChanged` tell you which list actually has new data since
    //  the last successful send (see model.ChangeTracker, wired up in AlertSystemController) --
    //  e.g. skip sending a list entirely if its flag is false.
    @Override
    public boolean sendUpdate(List<Company> companies, boolean companiesChanged,
                               List<AlertContact> alertContacts, boolean alertContactsChanged) throws Exception {
        Thread.sleep(1500);
        return Math.random() > 0.2;
    }
}
