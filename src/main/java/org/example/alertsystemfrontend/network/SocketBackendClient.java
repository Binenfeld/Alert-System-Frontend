package org.example.alertsystemfrontend.network;

import org.example.alertsystemfrontend.model.AlertContact;
import org.example.alertsystemfrontend.model.Company;

import java.util.List;

// Every method below is currently stubbed with a fake delay and a fake result so the rest of the
// app (startup, loading overlay, success/failure dialogs) is fully testable before the real
// backend exists. connect()/sendHistoricalCheckRequest()/sendCompaniesUpdate()/sendContactsUpdate()
// are all called from a background thread (via javafx.concurrent.Task, or -- for connect() only --
// from Application.init(), which the JavaFX docs guarantee is NOT the FX Application Thread), so
// blocking socket I/O anywhere here is safe and will NOT freeze the window.
//
// Known protocol details (from reading the backend -- AlertSystemBackend is a sibling project):
//   - Plain java.net.Socket to the backend's ServerSocket, currently hardcoded to port 3000.
//   - The server pushes its current data unprompted, immediately after accepting the connection,
//     BEFORE any opcode-framed message: [4-byte BE companies-length][4-byte BE contacts-length]
//     [companies.json][contacts.json] -- two independent JSON arrays, no wrapper object.
//   - After that, every client->server message on the same connection is opcode-framed:
//     [1-byte opcode][4-byte BE payload length][JSON payload]. Opcodes: HISTORY_CHECK = 1,
//     UPDATE_COMPANIES_LIST = 2, UPDATE_CONTACTS_LIST = 3.
//   - As of this writing the backend's update handlers are empty stubs with no ack mechanism, and
//     the header framing has a char/byte encoding bug for non-trivial payloads -- both need fixing
//     server-side before this can be wired up for real.
public class SocketBackendClient implements BackendClient {

    // TODO: called once from AlertSystemApplication.init(), before any window is shown. Replace
    //  this body with the real connection: open a Socket to the backend, read the initial push
    //  described above, Gson-deserialize each JSON blob directly into List<Company> and
    //  List<AlertContact> (no wrapper type), and keep the Socket/streams around as fields on this
    //  class so the methods below can reuse the same connection. Let this method throw on failure
    //  (the caller shows an error dialog and exits) rather than returning a fallback InitialData.
    @Override
    public InitialData connect() throws Exception {
        Thread.sleep(1000);
        return new InitialData(List.of(), List.of());
    }

    // TODO: called when the user fills in the "בדיקה היסטורית" dialog (Dialogs.showHistoricalCheckDialog)
    //  and presses "שלח". Replace this body with the real call: write an opcode-1 framed message
    //  built from `company`'s fields (company.nameProperty().get(), company.companyIdProperty().get(),
    //  company.peopleImpressionProperty().get(), company.financialImpressionProperty().get())
    //  serialized as JSON, over the connection opened in connect(). Read the backend's response
    //  and return true only if it confirms success.
    @Override
    public boolean sendHistoricalCheckRequest(Company company) throws Exception {
        Thread.sleep(1500);
        return Math.random() > 0.2;
    }

    // TODO: called when the user presses "שלח עדכון" and AlertSystemController's ChangeTracker
    //  says the companies list has changes. AlertSystemController.validateAll() has already
    //  confirmed every company is fully filled in and valid, so this data needs no further
    //  checking. Replace this body with the real call: Gson-serialize `companies` directly (no
    //  wrapper type) -- including each Company's comments via company.getComments() and unpaid
    //  months via company.unpaidMonthsProperty().get() -- into an opcode-2 framed message, send it
    //  over the connection opened in connect(), read back the confirmation, and return true only
    //  on success.
    @Override
    public boolean sendCompaniesUpdate(List<Company> companies) throws Exception {
        Thread.sleep(1500);
        return Math.random() > 0.2;
    }

    // TODO: same as sendCompaniesUpdate above, but for the alert contacts list: called only when
    //  the alert contacts ChangeTracker says it has changes, Gson-serialize `alertContacts`
    //  directly into an opcode-3 framed message, send it, and return true only on confirmed success.
    @Override
    public boolean sendContactsUpdate(List<AlertContact> alertContacts) throws Exception {
        Thread.sleep(1500);
        return Math.random() > 0.2;
    }

    // TODO: called once from AlertSystemApplication.stop(), when the application is closing.
    //  Replace this body with closing the Socket/streams opened in connect().
    @Override
    public void disconnect() throws Exception {
    }
}
