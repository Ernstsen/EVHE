package dk.mmj.evhe.server.decryptionauthority;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.eSoftware.commandLineParser.Configuration;
import dk.mmj.evhe.crypto.ElGamal;
import dk.mmj.evhe.crypto.SecurityUtils;
import dk.mmj.evhe.crypto.zeroknowledge.DLogProofUtils;
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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

        List<Class> classes = Arrays.asList(
                ArrayList.class,
                PartialResult.class,
                DLogProofUtils.Proof.class);
        bulletinBoard = configureWebTarget(logger, configuration.bulletinBoard, classes);

        File conf = new File(configuration.confPath);
        if (!conf.exists() || !conf.isFile()) {
            logger.error("Configuration file either did not exists or were not a file. Path: " + conf.getAbsolutePath() + "\nTerminating");
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
            long endTime = Long.parseLong(endTimeString);
            long relativeEndTime = endTime - new Date().getTime();

            scheduler.schedule(this::terminateVoting, relativeEndTime, TimeUnit.MILLISECONDS);

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
        PartialSecretKey key = state.get(SECRET_KEY, PartialSecretKey.class);
        PublicKey publicKey = state.get(PUBLIC_KEY, PublicKey.class);
        Long endTime = state.get(END_TIME, Long.class);

        logger.info("Terminating voting - Fetching votes");
        ArrayList<PersistedVote> votes = getVotes();

        if (votes == null || votes.size() < 1) {
            logger.error("No votes registered. Terminating server without result");
            terminate();
            return;
        }

        logger.info("Summing votes");
        CipherText sum = SecurityUtils.concurrentVoteSum(
                votes.parallelStream().filter(v -> v.getTs().getTime() < endTime).collect(Collectors.toList()),
                publicKey,
                1000);

        logger.info("Beginning partial decryption");


        BigInteger result = ElGamal.partialDecryption(sum.getC(), key.getSecretValue(), key.getP());

        logger.info("Partially decrypted value. Generating proof");

        PublicKey partialPublicKey = new PublicKey(publicKey.getG().modPow(key.getSecretValue(), key.getP()), publicKey.getG(), publicKey.getQ());
        DLogProofUtils.Proof proof = DLogProofUtils.generateProof(sum, key.getSecretValue(), partialPublicKey, id);

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

    private ArrayList<PersistedVote> getVotes() {
        try {
            String getVotes = bulletinBoard.path("getVotes").request().get(String.class);
            VoteList voteObjects = new ObjectMapper().readerFor(VoteList.class).readValue(getVotes);

            ArrayList<PersistedVote> votes = new ArrayList<>();
            for (Object vote : voteObjects.getVotes()) {
                if (vote instanceof PersistedVote) {
                    votes.add((PersistedVote) vote);
                } else {
                    logger.error("Found vote that was not ciphertext. Was " + vote.getClass() + ". Terminating server");
                    terminate();
                }
            }
            return votes;
        } catch (IOException e) {
            logger.error("Failed to read VoteList from JSON string", e);
            return null;
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