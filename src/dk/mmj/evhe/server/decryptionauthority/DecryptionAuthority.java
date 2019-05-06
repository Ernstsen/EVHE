package dk.mmj.evhe.server.decryptionauthority;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.eSoftware.commandLineParser.Configuration;
import dk.mmj.evhe.crypto.ElGamal;
import dk.mmj.evhe.crypto.zeroknowledge.DLogProofUtils;
import dk.mmj.evhe.crypto.zeroknowledge.VoteProofUtils;
import dk.mmj.evhe.entities.*;
import dk.mmj.evhe.server.AbstractServer;
import dk.mmj.evhe.server.ServerState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.client.JerseyWebTarget;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static dk.mmj.evhe.client.SSLHelper.configureWebTarget;

public class DecryptionAuthority extends AbstractServer {
    static final String SERVER = "server";
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final String SECRET_KEY = "secretKey";
    private static final String PUBLIC_KEY = "publicKey";
    private static final String END_TIME = "terminationTime";
    private static final Logger logger = LogManager.getLogger(DecryptionAuthority.class);
    private final ServerState state = ServerState.getInstance();
    private JerseyWebTarget bulletinBoard;
    private Integer id;
    private int port = 8081;

    public DecryptionAuthority(KeyServerConfiguration configuration) {
        if (configuration.port != null) {
            port = configuration.port;
        }

        bulletinBoard = configureWebTarget(logger, configuration.bulletinBoard, Collections.singletonList(ArrayList.class));

        File conf = new File(configuration.confPath);
        if (!conf.exists() || !conf.isFile()) {
            logger.error("Configuration file either did not exists or were not a file. Terminating");
            System.exit(-1);
        }


        try (FileInputStream ous = new FileInputStream(conf)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(ous));

            id = Integer.parseInt(reader.readLine());
            BigInteger secretValue = new BigInteger(reader.readLine());
            BigInteger p = new BigInteger(reader.readLine());
            String publicKeyString = reader.readLine();
            String endTimeString = reader.readLine();

            PublicKey pk = new ObjectMapper().readerFor(PublicKey.class).readValue(publicKeyString);


            PartialSecretKey sk = new PartialSecretKey(secretValue, p);
            long endTime = Long.parseLong(endTimeString) - new Date().getTime();

            scheduler.schedule(this::terminateVoting, endTime, TimeUnit.MILLISECONDS);

            state.put(END_TIME, endTime);
            state.put(PUBLIC_KEY, pk);
            state.put(SECRET_KEY, sk);
        } catch (JsonProcessingException e) {
            logger.error("Unable to deserialize public key. Terminating", e);
            System.exit(-1);
        } catch (FileNotFoundException e) {
            logger.error("Configuration file not found. Terminating", e);
            System.exit(-1);
        } catch (IOException e) {
            logger.error("Unable to read configuration file. Terminating", e);
            System.exit(-1);
        }

        state.put(SERVER, this);
    }

    void terminateVoting() {
        Response getVotes = bulletinBoard.path("getVotes").request().get();
        VoteList voteObjects = getVotes.readEntity(VoteList.class);

        PartialSecretKey key = state.get(SECRET_KEY, PartialSecretKey.class);
        PublicKey publicKey = state.get(PUBLIC_KEY, PublicKey.class);
        logger.info("Terminating voting - summing votes");

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

        BigInteger result = ElGamal.partialDecryption(sum.getC(), key.getSecretValue(), key.getP());

        logger.info("Partially decrypted value. Generating proof");

        DLogProofUtils.Proof proof = null;
        //TODO!

        logger.info("Posting to bulletin board");

        Entity<PartialResult> resultEntity = Entity.entity(new PartialResult(id, result, proof), MediaType.APPLICATION_JSON);
        Response post = bulletinBoard.path("result").request().post(resultEntity);

        if (post.getStatus() < 200 || post.getStatus() > 300) {
            logger.error("Unable to post result to bulletinBoard, got response:" + post);
            System.exit(-1);
        } else {
            logger.info("Successfully transferred partial decryption to bulletin board");
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
        private final Integer port;
        private String bulletinBoard;
        private String confPath;

        KeyServerConfiguration(Integer port, String bulletinBoard, String confPath) {
            this.port = port;
            this.bulletinBoard = bulletinBoard;
            this.confPath = confPath;
        }
    }
}