package org.example.alertsystemfrontend.network.ssl;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;

// Loads the frontend's TLS truststore (just the backend's self-signed public certificate, no
// private key) and builds the SSLSocketFactory that SocketBackendClient wraps its Socket with.
public final class SslConfig {

    private static final String TRUSTSTORE_RESOURCE = "/truststore/alertsystem.p12";
    private static final char[] TRUSTSTORE_PASSWORD = "alertsystem".toCharArray();

    private SslConfig() {
    }

    public static SSLSocketFactory createSocketFactory() throws Exception {
        KeyStore trustStore = KeyStore.getInstance("PKCS12");
        try (InputStream in = SslConfig.class.getResourceAsStream(TRUSTSTORE_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Missing truststore resource: " + TRUSTSTORE_RESOURCE);
            }
            trustStore.load(in, TRUSTSTORE_PASSWORD);
        }

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
        return sslContext.getSocketFactory();
    }
}
