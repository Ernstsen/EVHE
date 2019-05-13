package dk.mmj.evhe.server.decryptionauthority;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.eSoftware.commandLineParser.Configuration;
import dk.mmj.evhe.crypto.ElGamal;
import dk.mmj.evhe.crypto.SecurityUtils;
import dk.mmj.evhe.crypto.zeroknowledge.DLogProofUtils;
import dk.mmj.evhe.entities.*;
import dk.mmj.evhe.server.AbstractServer;
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
    private static final Logger logger = LogManager.getLogger(DecryptionAuthority.class);
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private JerseyWebTarget bulletinBoard;
    private boolean timeCorrupt = false;
    private PartialSecretKey sk;
    private int port = 8081;
    private long endTime;
    private PublicKey pk;
    private Integer id;

    public DecryptionAuthority(DecryptionAuthorityConfiguration configuration) {
        if (configuration.port != null) {
            port = configuration.port;
        }

        if (configuration.corrupt.equals("time")) {
            timeCorrupt = true;
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

            pk = new ObjectMapper().readerFor(PublicKey.class).readValue(publicKeyString);


            sk = new PartialSecretKey(secretValue, p);
            endTime = Long.parseLong(endTimeString);
            long relativeEndTime = endTime - new Date().getTime();

            if (timeCorrupt) {
                relativeEndTime -= 30000; //30 sec.
            }

            scheduler.schedule(this::terminateVoting, relativeEndTime, TimeUnit.MILLISECONDS);

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
    }

    private void terminateVoting() {
        Long bulletinBoardTime = new Long(bulletinBoard.path("getCurrentTime").request().get(String.class));
        long remainingTime = endTime - bulletinBoardTime;

        if (!timeCorrupt && remainingTime > 0) {
            logger.info("Attempted to collect votes from BB, but voting not finished. Retrying in " + (remainingTime / 1000) + "s");
            scheduler.schedule(this::terminateVoting, remainingTime, TimeUnit.MILLISECONDS);
            return;
        }

        logger.info("Terminating voting - Fetching votes");
        ArrayList<PersistedVote> votes = getVotes();

        if (votes == null || votes.size() < 1) {
            logger.error("No votes registered. Terminating server without result");
            terminate();
            return;
        }

        logger.info("Summing votes");
        List<PersistedVote> filteredVotes = votes.parallelStream().filter(v -> v.getTs().getTime() < endTime).collect(Collectors.toList());
        CipherText sum = SecurityUtils.concurrentVoteSum(
                filteredVotes,
                pk,
                1000);

        logger.info("Beginning partial decryption");


        BigInteger result = ElGamal.partialDecryption(sum.getC(), sk.getSecretValue(), sk.getP());

        logger.info("Partially decrypted value. Generating proof");

        PublicKey partialPublicKey = new PublicKey(pk.getG().modPow(sk.getSecretValue(), sk.getP()), pk.getG(), pk.getQ());
        DLogProofUtils.Proof proof = DLogProofUtils.generateProof(sum, sk.getSecretValue(), partialPublicKey, id);

        logger.info("Posting to bulletin board");

        Entity<PartialResult> resultEntity = Entity.entity(new PartialResult(id, result, proof, sum, filteredVotes.size()), MediaType.APPLICATION_JSON);
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
    public static class DecryptionAuthorityConfiguration implements Configuration {
        private final Integer port;
        private String bulletinBoard;
        private String confPath;
        private String corrupt;

        DecryptionAuthorityConfiguration(Integer port, String bulletinBoard, String confPath, String corrupt) {
            this.port = port;
            this.bulletinBoard = bulletinBoard;
            this.confPath = confPath;
            this.corrupt = corrupt;
        }
    }
}