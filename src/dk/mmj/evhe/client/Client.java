package dk.mmj.evhe.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.eSoftware.commandLineParser.Configuration;
import dk.mmj.evhe.Application;
import dk.mmj.evhe.crypto.CipherText;
import dk.mmj.evhe.crypto.ElGamal;
import dk.mmj.evhe.crypto.PublicKey;
import dk.mmj.evhe.server.VoteDTO;
import dk.mmj.evhe.server.keyServer.KeyServerConfigBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.JerseyWebTarget;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Random;
import java.util.UUID;

import static dk.mmj.evhe.server.AbstractServer.CERTIFICATE_PASSWORD;
import static dk.mmj.evhe.server.AbstractServer.CERTIFICATE_PATH;

public class Client implements Application {
    private static final Logger logger = LogManager.getLogger(KeyServerConfigBuilder.class);
    private JerseyWebTarget target;
    private String id;
    private Boolean vote;
    private Integer multi;

    public Client(ClientConfiguration configuration) {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.register(VoteDTO.class);
        clientConfig.register(PublicKey.class);

        id = configuration.id;
        vote = configuration.vote;
        multi = configuration.multi;
        try {

            // The following is needed for localhost testing.
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, sslSession) -> hostname.equals("localhost"));

            KeyStore keyStore = KeyStore.getInstance("jceks");
            keyStore.load(new FileInputStream(CERTIFICATE_PATH), CERTIFICATE_PASSWORD.toCharArray());
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);

            SSLContext ssl = SSLContext.getInstance("SSL");
            ssl.init(null, tmf.getTrustManagers(), new SecureRandom());

            JerseyClient client = (JerseyClient) JerseyClientBuilder.newBuilder().withConfig(clientConfig).sslContext(ssl).build();

            target = client.target(configuration.targetUrl);

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
    public void run() {
        assertPublicServer();

        PublicKey publicKey = getPublicKey();
        if (multi != null) {
            doMultiVote(publicKey);
        } else {
            int vote = getVote();
            doVote(publicKey, vote);
        }
    }

    private void doMultiVote(PublicKey publicKey) {
        Random random = new Random();
        int trueVotes = 0;
        int falseVotes = 0;
        for (int i = 0; i < multi; i++) {
            System.out.print("Dispatching votes: " + i + "/" + multi + " \r");
            id = UUID.randomUUID().toString();
            int vote = random.nextInt(2);
            if (vote == 0) {
                falseVotes++;
            } else {
                trueVotes++;
            }
            doVote(publicKey, vote);
        }
        System.out.println("Dispatched " + multi + " votes with " + trueVotes + " for, and " + falseVotes + " against");
    }

    private void doVote(PublicKey publicKey, int vote) {
        CipherText encryptedVote = ElGamal.homomorphicEncryption(publicKey, BigInteger.valueOf(vote));
        postVote(encryptedVote);
    }

    private void assertPublicServer() {
        // Check that we are connected to PublicServer
        Response publicServerResp = target.path("type").request().buildGet().invoke();

        if (publicServerResp.getStatus() != 200) {
            logger.error("Couldn't connect to the publicServer.");
            throw new RuntimeException("Failed : HTTP error code : " + publicServerResp.getStatus());
        }

        String responseEntity = publicServerResp.readEntity(String.class);

        if (!responseEntity.contains("Public Server")) {
            throw new RuntimeException("Server was not of type publicServer");
        }
    }

    private void postVote(CipherText encryptedVote) {
        try {
            VoteDTO payload = new VoteDTO(encryptedVote, id);
            Entity<?> entity = Entity.entity(new ObjectMapper().writeValueAsString(payload), MediaType.APPLICATION_JSON_TYPE);
            Response response = target.path("vote").request().post(entity);

            if (response.getStatus() != 204) {
                logger.warn("Failed to post vote to server: Error code was " + response.getStatus());
            }
        } catch (JsonProcessingException e) {
            logger.error("Unable write VoteDTO as JSON", e);
        }

    }

    private int getVote() {
        if (vote == null) {
            System.out.println("Please enter vote to be cast: true/false");
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            try {
                String s = reader.readLine();
                vote = Boolean.parseBoolean(s);
                System.out.println("voting: " + vote);
            } catch (IOException ignored) {
            }
        }

        if (vote) {
            return 1;
        } else {
            return 0;
        }


    }

    private PublicKey getPublicKey() {
        Response response = target.path("publicKey").request().buildGet().invoke();
        return response.readEntity(PublicKey.class);
    }

    public static class ClientConfiguration implements Configuration {
        private final String targetUrl;
        private final String id;
        private Boolean vote;
        private Integer multi;

        ClientConfiguration(String targetUrl, String id, Boolean vote, Integer multi) {
            this.targetUrl = targetUrl;
            this.id = id;
            this.vote = vote;
            this.multi = multi;
        }
    }
}
