package org.example.alertsystemfrontend.network;

import org.example.alertsystemfrontend.model.AlertContact;
import org.example.alertsystemfrontend.model.Company;

import java.util.List;

// Everything the UI needs from the backend connection. AlertSystemController only depends on
// this interface, never on SocketBackendClient directly, so the socket implementation can be
// swapped out (e.g. for a fake one in a test) without touching any UI code.
//
// The backend protocol is a single persistent connection: connect() opens it and receives the
// server's initial data push, and every later call (sendHistoricalCheckRequest, sendCompaniesUpdate,
// sendContactsUpdate) reuses that same connection until disconnect() closes it.
//
// The frontend only ever tells the backend what changed in the real world -- it never waits for
// or displays anything the backend does with that (any success/failure outcome is the alert
// contacts' business, delivered by email from the backend, not the frontend's). So these are
// fire-and-forget: they throw if the send itself failed (e.g. the connection is down), and return
// nothing otherwise.
public interface BackendClient {

    // Called once, before any window is shown (see AlertSystemApplication.init()).
    InitialData connect() throws Exception;

    void sendHistoricalCheckRequest(Company company) throws Exception;

    // Separate methods (rather than one combined "sendUpdate") because the backend protocol has
    // two independent opcodes, one per list -- see model.ChangeTracker for how the UI decides
    // which of these to call at all.
    void sendCompaniesUpdate(List<Company> companies) throws Exception;

    void sendContactsUpdate(List<AlertContact> alertContacts) throws Exception;

    // Called once, when the application is closing (see AlertSystemApplication.stop()).
    void disconnect() throws Exception;
}
