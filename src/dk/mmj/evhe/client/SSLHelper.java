package dk.mmj.evhe.client;

import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.JerseyWebTarget;

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
    private static SSLContext initializeSSL() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, KeyManagementException {
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

    /**
     * Sets up {@link javax.ws.rs.client.WebTarget} using SSL
     *
     * @param logger    logger used for reporting potential errors
     * @param targetUrl aseUrl for the webTarget
     * @return {@link JerseyWebTarget} for accessing server at targetUrl
     */
    public static JerseyWebTarget configureWebTarget(Logger logger, String targetUrl) {
        ClientConfig clientConfig = new ClientConfig();

        try {
            SSLContext ssl = SSLHelper.initializeSSL();

            JerseyClient client = (JerseyClient) JerseyClientBuilder.newBuilder()
                    .withConfig(clientConfig)
                    .sslContext(ssl)
                    .build();

            return client.target(targetUrl);

        } catch (NoSuchAlgorithmException e) {
            logger.error("Unrecognized SSL context algorithm:", e);
            System.exit(-1);
        } catch (KeyManagementException e) {
            logger.error("Initializing SSL Context failed: ", e);
            System.exit(-1);
        } catch (CertificateException | KeyStoreException | IOException e) {
            logger.error("Error Initializing the Certificate: ", e);
            System.exit(-1);
        }

        return null;
    }
}
