package dk.mmj.evhe.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.eSoftware.commandLineParser.Configuration;
import dk.mmj.evhe.Application;
import dk.mmj.evhe.crypto.CipherText;
import dk.mmj.evhe.crypto.ElGamal;
import dk.mmj.evhe.crypto.PublicKey;
import dk.mmj.evhe.crypto.Utils;
import dk.mmj.evhe.server.VoteDTO;
import dk.mmj.evhe.server.keyServer.KeyServerConfigBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.JerseyWebTarget;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Random;
import java.util.UUID;

public class Client implements Application {
    private static final Logger logger = LogManager.getLogger(KeyServerConfigBuilder.class);
    private JerseyWebTarget target;
    private String id;
    private Boolean vote;
    private Integer multi;

    /**
     * Creates a client instance, that utilizes the SSL protocol to communicate with the public server.
     *
     * @param configuration the ClientConfiguration built in the same class.
     */
    public Client(ClientConfiguration configuration) {

        id = configuration.id;
        vote = configuration.vote;
        multi = configuration.multi;

        configureWebTarget(configuration.targetUrl);
    }

    /**
     * Sets up {@link javax.ws.rs.client.WebTarget} using SSL
     *
     * @param targetUrl baseUrl for the webTarget
     */
    private void configureWebTarget(String targetUrl) {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.register(VoteDTO.class);
        clientConfig.register(PublicKey.class);

        try {
            SSLContext ssl = SSLHelper.initializeSSL();

            JerseyClient client = (JerseyClient) JerseyClientBuilder.newBuilder().withConfig(clientConfig).sslContext(ssl).build();

            target = client.target(targetUrl);

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
    }

    /**
     * Fetches the public key from the public server, and casts vote.
     * <br/>
     * if <code>multi</code> is set it casts <code>multi</code> random votes, for testing purposes.
     * <br/>
     * Otherwise just casts a single, specified vote.
     */
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

    /**
     * Casts <code>multi</code> random votes, for testing purposes.
     *
     * @param publicKey is the public key used to encrypt the vote.
     */
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

    /**
     * Encrypts the vote under the public key, and casts the encrypted vote.
     *
     * @param publicKey is the public key used to encrypt the vote.
     * @param vote      is the vote to be cast, either 0 or 1.
     */
    private void doVote(PublicKey publicKey, int vote) {
        BigInteger r = Utils.getRandomNumModN(publicKey.getQ());
        CipherText encryptedVote = ElGamal.homomorphicEncryption(publicKey, BigInteger.valueOf(vote), r);
        postVote(encryptedVote);
    }

    /**
     * Makes sure that the {@link javax.ws.rs.client.WebTarget} is a public-server, and that it is live.
     * <br/>
     * Throws a {@link RuntimeException} if this is not the case.
     */
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

    /**
     * Posts the encrypted vote to the public server, using the "/vote" path.
     *
     * @param encryptedVote the vote encrypted under the public key.
     */
    private void postVote(CipherText encryptedVote) {
        try {
            VoteDTO payload = new VoteDTO(encryptedVote, id, null);
            Entity<?> entity = Entity.entity(new ObjectMapper().writeValueAsString(payload), MediaType.APPLICATION_JSON_TYPE);
            Response response = target.path("vote").request().post(entity);

            if (response.getStatus() != 204) {
                logger.warn("Failed to post vote to server: Error code was " + response.getStatus());
            }
        } catch (JsonProcessingException e) {
            logger.error("Unable write VoteDTO as JSON", e);
        }
    }

    /**
     * Retrieves vote to be cast
     * <br/>
     * If none is defined, terminal is prompted for one
     * <br/>
     * Valid inputs for the vote is either "true" or "false".
     *
     * @return 1 or 0 according to input.
     */
    private int getVote() {
        if (vote == null) {
            System.out.println("Please enter vote to be cast: true/false");
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            try {
                String s = reader.readLine();
                vote = Boolean.parseBoolean(s);
                System.out.println("voting: " + vote);
            } catch (IOException ignored) {
                System.out.println("Unable to read vote - terminating");
                System.exit(-1);
            }
        }

        if (vote) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * Fetches the public key by requesting it from the public servers "/publicKey" path.
     *
     * @return the response containing the Public Key.
     */
    private PublicKey getPublicKey() {
        Response response = target.path("publicKey").request().buildGet().invoke();

        return response.readEntity(PublicKey.class);
    }

    /**
     * The Client Configuration.
     * <br/>
     * Created in the {@link ClientConfigBuilder}.
     */
    public static class ClientConfiguration implements Configuration {
        private final String targetUrl;
        private final String id;
        private final Boolean vote;
        private final Integer multi;

        ClientConfiguration(String targetUrl, String id, Boolean vote, Integer multi) {
            this.targetUrl = targetUrl;
            this.id = id;
            this.vote = vote;
            this.multi = multi;
        }
    }
}
