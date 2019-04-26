package dk.mmj.evhe.server.decryptionauthority;

import dk.eSoftware.commandLineParser.Configuration;
import dk.mmj.evhe.client.SSLHelper;
import dk.mmj.evhe.crypto.ElGamal;
import dk.mmj.evhe.entities.CipherText;
import dk.mmj.evhe.entities.KeyPair;
import dk.mmj.evhe.entities.PublicKey;
import dk.mmj.evhe.crypto.exceptions.UnableToDecryptException;
import dk.mmj.evhe.crypto.keygeneration.KeyGenerationParameters;
import dk.mmj.evhe.crypto.keygeneration.KeyGenerationParametersImpl;
import dk.mmj.evhe.crypto.zeroknowledge.VoteProofUtils;
import dk.mmj.evhe.server.AbstractServer;
import dk.mmj.evhe.server.ServerState;
import dk.mmj.evhe.entities.VoteDTO;
import dk.mmj.evhe.entities.VoteList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.JerseyWebTarget;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;

public class DecryptionAuthority extends AbstractServer {
    static final String KEY_PAIR = "keypair";
    static final String SERVER = "server";
    private static final Logger logger = LogManager.getLogger(DecryptionAuthority.class);
    private final ServerState state = ServerState.getInstance();
    private JerseyWebTarget bulletinBoard;
    private int port = 8081;

    public DecryptionAuthority(KeyServerConfiguration configuration) {
        if (configuration.port != null) {
            port = configuration.port;
        }

        KeyPair keyPair;

        if (configuration.keygenParams != null) {
            KeyGenerationParameters params = configuration.keygenParams;
            keyPair = ElGamal.generateKeys(params);
        } else {
            KeyGenerationParametersImpl params = new KeyGenerationParametersImpl(1024, 50);
            keyPair = ElGamal.generateKeys(params);
        }

        try {
            SSLContext ssl = SSLHelper.initializeSSL();

            JerseyClient client = (JerseyClient) JerseyClientBuilder.newBuilder().sslContext(ssl).build();

            client.register(ArrayList.class);

            bulletinBoard = client.target(configuration.bulletinBoard);
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException | KeyManagementException e) {
            logger.error("Unable to connect to bulletin Board", e);
            terminate();
        }

        state.put(KEY_PAIR, keyPair);
        configureWebTarget(configuration);

        postPublicKey();
        state.put(SERVER, this);
    }

    private void postPublicKey() {
        KeyPair keyPair = ServerState.getInstance().get(KEY_PAIR, KeyPair.class);
        Entity<PublicKey> entity = Entity.entity(keyPair.getPublicKey(), MediaType.APPLICATION_JSON);

        Response resp = bulletinBoard.path("publicKey").request().post(entity);
        if (resp.getStatus() < 200 || resp.getStatus() > 300) {
            logger.error("Unable to post publickey to bulletinBoard, got response:" + resp);
            System.exit(-1);
        }
    }

    void terminateVoting() {
        Response getVotes = bulletinBoard.path("getVotes").request().get();
        VoteList voteObjects = getVotes.readEntity(VoteList.class);

        PublicKey publicKey = state.get(KEY_PAIR, KeyPair.class).getPublicKey();
        logger.info("Terminating voting - adding votes");

        ArrayList<VoteDTO> votes = new ArrayList<>();

        for (Object vote : voteObjects.getVotes()) {
            if (vote instanceof VoteDTO) {
                votes.add((VoteDTO) vote);
            } else {
                logger.error("Found vote that was not ciphertext. Was " + vote.getClass() + ". Terminating server");
                terminate();
            }
        }

        if (votes.size() < 1) {
            logger.error("No votes registered. Terminating server without result");
            terminate();
        }

        CipherText acc = ElGamal.homomorphicEncryption(publicKey, BigInteger.ZERO);

        CipherText sum = votes.stream()
                .filter(v -> VoteProofUtils.verifyProof(v, publicKey))
                .map(VoteDTO::getCipherText)
                .reduce(acc, ElGamal::homomorphicAddition);

        logger.info("Dispatching decryption request");

        KeyPair keyPair = state.get(KEY_PAIR, KeyPair.class);
        int result = 0;
        try {
            result = ElGamal.homomorphicDecryption(keyPair, sum, 1000);
        } catch (UnableToDecryptException e) {
            logger.error("Failed to decrypt result. Terminating");
            System.exit(-1);
        }

        String resultString = "Result was: " + result + " with " + votes.size() + " votes";
        logger.info(resultString);

        Entity<String> resultEntity = Entity.entity(resultString, MediaType.APPLICATION_JSON);
        Response post = bulletinBoard.path("result").request().post(resultEntity);

        if (post.getStatus() < 200 || post.getStatus() > 300) {
            logger.error("Unable to post result to bulletinBoard, got response:" + post);
            System.exit(-1);
        }

    }

    private void configureWebTarget(KeyServerConfiguration configuration) {
        try {
            SSLContext ssl = SSLHelper.initializeSSL();

            JerseyClient client = (JerseyClient) JerseyClientBuilder.newBuilder().sslContext(ssl).build();

            bulletinBoard = client.target(configuration.bulletinBoard);

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
                DecryptionAuthorityResource.class.getCanonicalName());
    }

    @Override
    protected int getPort() {
        return port;
    }

    /**
     * Configuration for a DecryptionAuthority
     */
    public static class KeyServerConfiguration implements Configuration {
        private final KeyGenerationParameters keygenParams;
        private final Integer port;
        private String bulletinBoard;

        KeyServerConfiguration(Integer port, KeyGenerationParameters keygenParams, String bulletinBoard) {
            this.port = port;
            this.keygenParams = keygenParams;
            this.bulletinBoard = bulletinBoard;
        }

    }
}