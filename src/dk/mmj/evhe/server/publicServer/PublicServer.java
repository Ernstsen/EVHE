package dk.mmj.evhe.server.publicServer;


import dk.eSoftware.commandLineParser.Configuration;
import dk.mmj.evhe.crypto.CipherText;
import dk.mmj.evhe.crypto.ElGamal;
import dk.mmj.evhe.crypto.PublicKey;
import dk.mmj.evhe.server.AbstractServer;
import dk.mmj.evhe.server.ServerState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.JerseyWebTarget;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashSet;


public class PublicServer extends AbstractServer {
    static final String PUBLIC_KEY = "publicKey";
    static final String VOTES = "votes";
    static final String HAS_VOTED = "hasVoted";
    static final String SERVER = "server";
    static final String IS_TEST = "isTesting";
    static final String RESULT = "finished";

    private static final Logger logger = LogManager.getLogger(PublicServer.class);
    private JerseyWebTarget keyServer;
    private PublicServerConfiguration configuration;
    private ServerState state = ServerState.getInstance();

    public PublicServer(PublicServerConfiguration configuration) {
        this.configuration = configuration;

        try {
            // The following is needed for localhost testing.
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, sslSession) -> hostname.equals("localhost"));

            KeyStore keyStore = KeyStore.getInstance("jceks");
            keyStore.load(new FileInputStream(CERTIFICATE_PATH), CERTIFICATE_PASSWORD.toCharArray());
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);

            SSLContext ssl = SSLContext.getInstance("SSL");
            ssl.init(null, tmf.getTrustManagers(), new SecureRandom());
            JerseyClient client = (JerseyClient) JerseyClientBuilder.newBuilder().sslContext(ssl).build();
            keyServer = client.target(configuration.keyServer);
            state.put(IS_TEST, configuration.test);

        } catch (NoSuchAlgorithmException e) {
            logger.error("Unrecognized SSL context algorithm:", e);
            System.exit(-1);
        } catch (KeyManagementException e) {
            logger.error("Initializing SSL Context failed: ", e);
        } catch (CertificateException | KeyStoreException | IOException e) {
            logger.error("Error Initializing the Certificate: ", e);
        }
    }

    @Override
    protected void configure(ServletHolder servletHolder) {
        servletHolder.setInitParameter(
                "jersey.config.server.provider.classnames",
                PublicServerResource.class.getCanonicalName() + ";" + "org.glassfish.jersey.jackson.JacksonFeature");

        retrievePublicKey();
        initializeVoting();
    }

    private void initializeVoting() {
        ServerState.getInstance().put(VOTES, new ArrayList<CipherText>());
        ServerState.getInstance().put(HAS_VOTED, new HashSet<String>());
        ServerState.getInstance().put(SERVER, this);
    }

    private void retrievePublicKey() {
        Response response = keyServer.path("publicKey").request().buildGet().invoke();

        if (response.getStatus() != 200) {
            RuntimeException e = new RuntimeException("Error when getting Public Key with status " + response.getStatus());
            logger.error("Unable to get PublicKey ", e);
            throw e;
        }

        PublicKey key = response.readEntity(PublicKey.class);

        state.put(PUBLIC_KEY, key);
    }

    void terminateVoting() {
        ArrayList voteObjects = state.get(VOTES, ArrayList.class);
        logger.info("Terminating voting - adding votes");

        ArrayList<CipherText> votes = new ArrayList<>();

        for (Object vote : voteObjects) {
            if (vote instanceof CipherText) {
                votes.add((CipherText) vote);
            } else {
                logger.error("Found vote that was not ciphertext. Was " + vote.getClass() + ". Terminating server");
                terminate();
            }
        }

        if (votes.size() < 1) {
            logger.error("No votes registered. Terminating server without result");
            terminate();
        }

        CipherText first = votes.remove(0);
        CipherText sum = votes.stream().reduce(first, ElGamal::homomorphicAddition);

        logger.info("Dispatching decryption request");

        Entity<CipherText> entity = Entity.entity(sum, MediaType.APPLICATION_JSON);
        Response response = keyServer.path("result").request().post(entity);

        if (response.getStatus() != 200) {
            logger.error("Failed to decrypt result. Response status was " + response.getStatus() + ". Terminating.");
            terminate();
        }

        BigInteger result = response.readEntity(BigInteger.class);

        String resultString = "Result was: " + result.toString() + " with " + (votes.size() + 1) + " votes";
        logger.info(resultString);
        state.put(RESULT, resultString);
    }

    @Override
    protected int getPort() {
        return configuration.port;
    }

    public static class PublicServerConfiguration implements Configuration {
        private Integer port;
        private String keyServer;
        private boolean test;

        PublicServerConfiguration(Integer port, String keyServer, boolean test) {
            this.port = port;
            this.keyServer = keyServer;
            this.test = test;
        }
    }
}