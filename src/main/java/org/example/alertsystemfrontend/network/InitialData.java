package org.example.alertsystemfrontend.network;

import org.example.alertsystemfrontend.model.AlertContact;
import org.example.alertsystemfrontend.model.Company;

import java.util.List;

// What the server pushes immediately upon connecting: the current companies and alert contacts.
// Purely an in-memory holder for passing both lists out of BackendClient.connect() -- it is never
// itself serialized. On the wire these are two independent JSON payloads (companies.json and
// contacts.json), not a wrapped object.
public record InitialData(List<Company> companies, List<AlertContact> alertContacts) {
}
