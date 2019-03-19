package dk.mmj.evhe.client;

import org.eclipse.jetty.util.ssl.SslContextFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;


public class SSLHelper {
    private static final String CERTIFICATE_PATH = "certs/keystore.jks";
    private static final String CERTIFICATE_PASSWORD = "password";

    /**
     * Initializes SSL locally and returns an {@link SSLContext} for a jersey client to use
     *
     * @return SSLContext for client use
     * @throws KeyStoreException        when KeyStore cannot retrieve the requested instance
     * @throws IOException              when Certificate cannot be read from disk
     * @throws NoSuchAlgorithmException when TrustManagerFactory, SSLContext or keystore are unable to load requested algorithm
     * @throws CertificateException     when one or more of the certificates in the keystore could not be loaded
     * @throws KeyManagementException   when ssl initialization fails
     */
    public static SSLContext initializeSSL() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, KeyManagementException {
        // Needed for localhost testing.
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, sslSession) -> hostname.equals("localhost"));

        KeyStore keyStore = KeyStore.getInstance("jceks");
        keyStore.load(new FileInputStream(CERTIFICATE_PATH), CERTIFICATE_PASSWORD.toCharArray());
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);

        SSLContext ssl = SSLContext.getInstance("SSL");
        ssl.init(null, tmf.getTrustManagers(), new SecureRandom());
        return ssl;
    }

    /**
     * Creates and initializes an SSLFactory
     *
     * @return an SslContextFactory for server use
     */
    public static SslContextFactory getSSLContextFactory() {
        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setKeyStorePath(CERTIFICATE_PATH);
        sslContextFactory.setKeyStorePassword(CERTIFICATE_PASSWORD);
        sslContextFactory.setKeyManagerPassword(CERTIFICATE_PASSWORD);
        sslContextFactory.setEndpointIdentificationAlgorithm("HTTPS");
        return sslContextFactory;
    }
}
