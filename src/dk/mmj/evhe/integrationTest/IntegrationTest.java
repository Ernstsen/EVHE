package dk.mmj.evhe.integrationTest;

import dk.eSoftware.commandLineParser.*;
import dk.mmj.evhe.Application;
import dk.mmj.evhe.client.ClientConfigBuilder;
import dk.mmj.evhe.client.ResultFetcher;
import dk.mmj.evhe.client.Voter;
import dk.mmj.evhe.initialization.TrustedDealer;
import dk.mmj.evhe.initialization.TrustedDealerConfigBuilder;
import dk.mmj.evhe.server.bulletinboard.BulletinBoard;
import dk.mmj.evhe.server.bulletinboard.BulletinBoardConfigBuilder;
import dk.mmj.evhe.server.decryptionauthority.DecryptionAuthority;
import dk.mmj.evhe.server.decryptionauthority.DecryptionAuthorityConfigBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * IntegrationTest runs a {@link BulletinBoard}, {@link dk.mmj.evhe.initialization.TrustedDealer} and a number of
 * {@link dk.mmj.evhe.server.decryptionauthority.DecryptionAuthority}s, as honest participants of the system.
 * <p>
 * Using the configuration it is possible to determine which ones should be executed and whether votes should
 * be case at the beginning, and whether they should be cast 30 secs. before planned termination
 */
public class IntegrationTest implements Application {
    private static final Logger logger = LogManager.getLogger(IntegrationTest.class);
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private List<Integer> decryptionAuthorities;
    private List<Integer> voteDelays;
    private int duration;

    public IntegrationTest(IntegrationTest.IntegrationTestConfiguration configuration) {
        this.decryptionAuthorities = configuration.decryptionAuthorities;
        this.duration = configuration.duration;
        this.voteDelays = configuration.voteDelays;
    }

    @Override
    public void run() {
        logger.warn("############### WARNING ###############");
        logger.warn("This is an integration test.");
        logger.warn("By using this configuration you are running the system with a single point of failure, namely this machine");
        logger.warn("Under no circumstance should this happen in a production environment");
        logger.warn("############### WARNING ###############");

        logger.info("Launching bulletinboard");
        startBulletinBoard();

        logger.info("Executing trusted dealer");
        runTrustedDealer(duration);

        for (Integer id : decryptionAuthorities) {
            logger.info("Launching decryption with id=" + id);
            launchDecryptionAuthority(id);
        }

        for (Integer voteDelay : voteDelays) {
            doMultiVote(voteDelay);
        }

        retrieveVotes(duration);
    }

    /**
     * Retrieves votes from bulletin board at later time
     *
     * @param timeOffset when to retrieve the votes in minutes
     */
    private void retrieveVotes(int timeOffset) {
        CommandLineParser parser = new SingletonCommandLineParser(new ClientConfigBuilder());
        Configuration parse;
        try {
            parse = parser.parse("--client --read=true".split(" "));
        } catch (NoSuchBuilderException | WrongFormatException e) {
            throw new RuntimeException("Failed parsing resultFetcher conf", e);
        }

        ResultFetcher voter = new ResultFetcher((ResultFetcher.ResultFetcherConfiguration) parse);

        scheduler.schedule(voter, timeOffset, TimeUnit.MINUTES);
    }

    /**
     * Asynchronously casts 50 random votes at specified relative time
     *
     * @param timeOffset relative delay for votes in ms
     */
    private void doMultiVote(int timeOffset) {
        CommandLineParser parser = new SingletonCommandLineParser(new ClientConfigBuilder());
        Configuration parse;
        try {
            parse = parser.parse("--client --multi=50 --port=8080".split(" "));
        } catch (NoSuchBuilderException | WrongFormatException e) {
            throw new RuntimeException("Failed parsing multivote conf", e);
        }

        Voter voter = new Voter((Voter.VoterConfiguration) parse);

        scheduler.schedule(voter, timeOffset, TimeUnit.MILLISECONDS);
    }

    private void startBulletinBoard() {
        CommandLineParser parser = new SingletonCommandLineParser(new BulletinBoardConfigBuilder());
        Configuration conf;
        try {
            conf = parser.parse(new String[0]);
        } catch (NoSuchBuilderException | WrongFormatException e) {
            throw new RuntimeException("Failed parsing bulletin board conf", e);
        }

        new Thread(new BulletinBoard((BulletinBoard.BulletinBoardConfiguration) conf)).start();
    }

    /**
     * Executes the trused dealer in thread, as to wait for finished execution
     *
     * @param duration duration of vote
     */
    private void runTrustedDealer(int duration) {
        String params = "--dealer --servers=3 --degree=1 --root=initFiles --keyPath=rsa --time -min=" + duration;
        CommandLineParser parser = new SingletonCommandLineParser(new TrustedDealerConfigBuilder());

        Configuration conf;
        try {
            conf = parser.parse(params.split(" "));
        } catch (NoSuchBuilderException | WrongFormatException e) {
            throw new RuntimeException("Failed parsing trusted dealer conf.", e);
        }

        new TrustedDealer((TrustedDealer.TrustedDealerConfiguration) conf).run();
    }

    private void launchDecryptionAuthority(Integer id) {
        String params = "--authority --conf=initFiles/" + id + " --port=808" + id;
        SingletonCommandLineParser parser = new SingletonCommandLineParser(new DecryptionAuthorityConfigBuilder());
        Configuration conf;
        try {
            conf = parser.parse(params.split(" "));
        } catch (NoSuchBuilderException | WrongFormatException e) {
            throw new RuntimeException("Failed parsing decryption authority conf.", e);
        }

        new Thread(new DecryptionAuthority((DecryptionAuthority.DecryptionAuthorityConfiguration) conf)).start();
    }

    public static class IntegrationTestConfiguration implements Configuration {
        private List<Integer> decryptionAuthorities;
        private List<Integer> voteDelays;
        private int duration;

        /**
         * @param decryptionAuthorities list of ids for all enabled decryption authorities
         * @param duration              how long the vote should be active(minutes)
         * @param voteDelays            list of times where votes should be dispatched
         */
        IntegrationTestConfiguration(List<Integer> decryptionAuthorities, int duration, List<Integer> voteDelays) {
            this.decryptionAuthorities = decryptionAuthorities;
            this.duration = duration;
            this.voteDelays = voteDelays;
        }
    }
}
