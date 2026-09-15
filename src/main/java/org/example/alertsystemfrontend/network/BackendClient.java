package org.example.alertsystemfrontend.network;

import org.example.alertsystemfrontend.model.AlertContact;
import org.example.alertsystemfrontend.model.Company;

import java.util.List;

// Everything the UI needs from the backend connection. AlertSystemController only depends on
// this interface, never on SocketBackendClient directly, so the socket implementation can be
// swapped out (e.g. for a fake one in a test) without touching any UI code.
public interface BackendClient {

    boolean sendHistoricalCheckRequest(Company company) throws Exception;

    // companiesChanged/alertContactsChanged say which list has additions, removals, or edited
    // items since the last successful sendUpdate() call (see model.ChangeTracker) - use them to
    // decide whether a list needs sending at all, or to send a partial vs. full payload.
    boolean sendUpdate(List<Company> companies, boolean companiesChanged,
                        List<AlertContact> alertContacts, boolean alertContactsChanged) throws Exception;
}
