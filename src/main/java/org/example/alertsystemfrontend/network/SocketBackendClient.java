package org.example.alertsystemfrontend.network;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.example.alertsystemfrontend.model.AlertContact;
import org.example.alertsystemfrontend.model.ChangeTracker;
import org.example.alertsystemfrontend.model.Company;
import org.example.alertsystemfrontend.network.json.GsonProvider;
import org.example.alertsystemfrontend.network.ssl.SslConfig;
import org.example.alertsystemfrontend.ui.AlertSystemController;

import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;

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

    public static final int PORT = 3000;
    public static final String HOST = "AlertSystem";
    private static final Gson GSON = GsonProvider.get();
    private Socket socket;

    public static enum OpCode {
        HISTORY_CHECK(1),
        UPDATE_COMPANIES_LIST(2),
        UPDATE_CONTACTS_LIST(3);

        // explicit wire value, kept separate from ordinal() so reordering/adding
        // enum constants later can't silently change what's sent over the wire
        private final int opcodeNumber;

        private OpCode(int opcodeNumber) {
            this.opcodeNumber = opcodeNumber;
        }
    }

    // Builds one opcode-framed message exactly as the backend's
    // Threads.SessionThread.startSessionAndWaitForCommands() reads it off the wire:
    // [1-byte opcode][4-byte big-endian length][UTF-8 payload bytes]. DataOutputStream.writeInt
    // writes big-endian, matching DataInputStream.readInt() on the read side, and the length
    // written is payloadBytes.length -- the UTF-8 *byte* count, not message.length() (its char
    // count) -- since those two diverge for any non-ASCII text (e.g. the Hebrew in these
    // payloads), which is exactly the header-framing bug noted in this class's file comment.
    private static byte[] encodeMessage(String message, OpCode opcode) throws IOException {
        byte[] payloadBytes = message.getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeByte(opcode.opcodeNumber);
        dos.writeInt(payloadBytes.length);
        dos.write(payloadBytes);
        return baos.toByteArray();
    }

    // TODO: called once from AlertSystemApplication.init(), before any window is shown. Replace
    //  this body with the real connection: open a Socket to the backend, read the initial push
    //  described above, Gson-deserialize each JSON blob directly into List<Company> and
    //  List<AlertContact> (no wrapper type), and keep the Socket/streams around as fields on this
    //  class so the methods below can reuse the same connection. Let this method throw on failure
    //  (the caller shows an error dialog and exits) rather than returning a fallback InitialData.
    @Override
    public InitialData connect() throws Exception {

        SSLSocketFactory factory = SslConfig.createSocketFactory();
        SSLSocket sslSocket = (SSLSocket) factory.createSocket(HOST, PORT);
        SSLParameters sslParameters = sslSocket.getSSLParameters();
        sslParameters.setEndpointIdentificationAlgorithm("HTTPS");
        sslSocket.setSSLParameters(sslParameters);
        sslSocket.startHandshake();
        this.socket = sslSocket;
        InitialData defRetValue = new InitialData(List.of(), List.of());
        List<Company> companies = List.of();
        List<AlertContact> contacts = List.of();
        try {
            DataInputStream dis = new DataInputStream(socket.getInputStream());


            int lengthCompanies, lengthContacts;
            try {
                lengthCompanies = dis.readInt();
                lengthContacts = dis.readInt();
            } catch (EOFException e) {
                System.out.println("connection close");
                return defRetValue;
            }

            // everything after the header is the payload, exactly `length` UTF-8 bytes
            byte[] companiesPayloadBytes = new byte[lengthCompanies];
            byte[] contactsPayloadBytes = new byte[lengthContacts];
            try {
                dis.readFully(companiesPayloadBytes);
                dis.readFully(contactsPayloadBytes);
            } catch (EOFException e) {
                System.out.println("connection close");
                return defRetValue;
            }

            String companiesJson = new String(companiesPayloadBytes, StandardCharsets.UTF_8);
            String contactsJson = new String(contactsPayloadBytes, StandardCharsets.UTF_8);
            Type companyListToken = new TypeToken<List<Company>>(){}.getType();
            Type contactListToken = new TypeToken<List<AlertContact>>(){}.getType();

            companies= GSON.fromJson(companiesJson, companyListToken);
            contacts= GSON.fromJson(contactsJson, contactListToken);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return new InitialData(companies, contacts);
    }

    // TODO: called when the user fills in the "בדיקה היסטורית" dialog (Dialogs.showHistoricalCheckDialog)
    //  and presses "שלח". Replace this body with the real call: write an opcode-1 framed message
    //  built from `company`'s fields (company.nameProperty().get(), company.companyIdProperty().get(),
    //  company.peopleImpressionProperty().get(), company.financialImpressionProperty().get())
    //  serialized as JSON, over the connection opened in connect(). Fire-and-forget -- let this
    //  throw if the write itself fails, but don't wait for or interpret any response; what the
    //  backend does with the request (and telling anyone the outcome) is entirely its own business.
    @Override
    public void sendHistoricalCheckRequest(Company company) throws Exception {
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        String json = GSON.toJson(company);
        byte[] message = encodeMessage(json, OpCode.HISTORY_CHECK);
        dos.write(message);
        dos.flush();
    }

    // TODO: called when the user presses "שלח עדכון" and AlertSystemController's ChangeTracker
    //  says the companies list has changes. AlertSystemController.validateAll() has already
    //  confirmed every company is fully filled in and valid, so this data needs no further
    //  checking. Replace this body with the real call: Gson-serialize `companies` directly (no
    //  wrapper type) -- including each Company's comments via company.getComments() and unpaid
    //  months via company.unpaidMonthsProperty().get() -- into an opcode-2 framed message, and
    //  send it over the connection opened in connect(). Fire-and-forget, same as above.
    @Override
    public void sendCompaniesUpdate(List<Company> companies) throws Exception {
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        String json = GSON.toJson(companies);
        byte[] message = encodeMessage(json, OpCode.UPDATE_COMPANIES_LIST);
        dos.write(message);
        dos.flush();
    }

    // TODO: same as sendCompaniesUpdate above, but for the alert contacts list: called only when
    //  the alert contacts ChangeTracker says it has changes, Gson-serialize `alertContacts`
    //  directly into an opcode-3 framed message and send it. Fire-and-forget, same as above.
    @Override
    public void sendContactsUpdate(List<AlertContact> alertContacts) throws Exception {
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        String json = GSON.toJson(alertContacts);
        byte[] message = encodeMessage(json, OpCode.UPDATE_CONTACTS_LIST);
        dos.write(message);
        dos.flush();
    }

    // TODO: called once from AlertSystemApplication.stop(), when the application is closing.
    //  Unlike every other method here, this one runs ON the JavaFX Application Thread (that's
    //  where Application.stop() always runs), so keep it quick -- just closing the Socket/streams
    //  opened in connect() -- rather than anything slow, since it can delay app shutdown.
    @Override
    public void disconnect() throws Exception {
        socket.close();
    }
}
