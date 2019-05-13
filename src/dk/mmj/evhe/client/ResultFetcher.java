package dk.mmj.evhe.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.mmj.evhe.crypto.ElGamal;
import dk.mmj.evhe.crypto.SecurityUtils;
import dk.mmj.evhe.crypto.exceptions.UnableToDecryptException;
import dk.mmj.evhe.crypto.zeroknowledge.DLogProofUtils;
import dk.mmj.evhe.crypto.zeroknowledge.VoteProofUtils;
import dk.mmj.evhe.entities.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class for retrieving vote results
 */
public class ResultFetcher extends Client {
    private static final Logger logger = LogManager.getLogger(ResultFetcher.class);
    private boolean forceCalculation;

    public ResultFetcher(ResultFetcherConfiguration configuration) {
        super(configuration);
        this.forceCalculation = configuration.forceCalculations;
    }

    @Override
    public void run() {
        logger.info("Fetching public information");
        PublicInformationEntity publicInformationEntity = fetchPublicInfo();
        long endTime = publicInformationEntity.getEndTime();
        if (new Date().getTime() < endTime) {
            long diff = (endTime - new Date().getTime()) / 60_000;
            logger.info("The vote has not yet terminated, so results are unavailable. " +
                    "The vote should terminate in about " + diff + " minutes");
            return;
        }

        logger.info("Fetching partial results");
        PublicKey publicKey = getPublicKey();
        ResultList resultList = target.path("result").request().get(ResultList.class);
        List<PartialResult> results = resultList.getResults();
        if (results == null || results.isEmpty()) {
            logger.info("Did not fetch any results. Probable cause is unfinished decryption. Try again later");
            return;
        }

        if (!forceCalculation) {
            logger.info("Checking if fetched ciphertexts and amount of collected votes match");
        }
        boolean decryptionAuthoritiesAgrees = decryptionAuthoritiesAgrees(results);
        PartialResult firstDA = results.get(0);

        CipherText sum = firstDA.getCipherText();
        BigInteger d = sum.getD();

        int amountOfVotes = firstDA.getVotes();

        if (forceCalculation || !decryptionAuthoritiesAgrees) {
            if (!decryptionAuthoritiesAgrees) {
                logger.info("Fetched partial results differed in ciphertexts or amount of collected votes. Computing own.");
            }
            if (forceCalculation) {
                logger.info("Forcing local calculations on votes");
            }

            logger.info("Fetching votes");
            VoteList votes;
            List<PersistedVote> actualVotes;
            try {
                String getVotes = target.path("getVotes").request().get(String.class);
                votes = new ObjectMapper().readerFor(VoteList.class).readValue(getVotes);
                logger.debug("Filtering votes");
                actualVotes = votes.getVotes().parallelStream()
                        .filter(v -> v.getTs().getTime() < endTime)
                        .filter(v -> VoteProofUtils.verifyProof(v, publicKey))
                        .collect(Collectors.toList());
            } catch (IOException e) {
                logger.error("Failed to read votes from server", e);
                System.exit(-1);
                return;
            }

            logger.info("Summing votes");
            sum = SecurityUtils.concurrentVoteSum(votes.getVotes(), publicKey, 1000);
            d = sum.getD();
            amountOfVotes = actualVotes.size();
        } else {
            logger.info("Fetched ciphertexts and number of votes were equal");
            logger.info("Using ciphertext and amount of collected votes from DA with id=" + firstDA.getId());
        }


        Map<Integer, BigInteger> partials = new HashMap<>();
        logger.info("Combining partials to total result");
        for (PartialResult result : results) {
            CipherText partialDecryption = new CipherText(result.getResult(), d);
            PublicKey partialPublicKey = new PublicKey(
                    publicInformationEntity.getPublicKeys().get(result.getId()),
                    publicInformationEntity.getG(),
                    publicInformationEntity.getQ());
            boolean validProof = DLogProofUtils.verifyProof(sum, partialDecryption, partialPublicKey, result.getProof(), result.getId());

            if (validProof) {
                partials.put(result.getId(), result.getResult());
            }
        }

        int result = 0;
        try {
            logger.info("Attempting to decrypt from " + partials.size() + " partials");
            BigInteger cs = SecurityUtils.combinePartials(partials, publicKey.getP());
            result = ElGamal.homomorphicDecryptionFromPartials(d, cs, publicKey.getG(), publicKey.getP(), amountOfVotes);
        } catch (UnableToDecryptException e) {
            logger.error("Failed to decrypt from partial decryptions.", e);
            System.exit(-1);
        }

        logger.info("Result: " + result + "/" + amountOfVotes);
    }

    private boolean decryptionAuthoritiesAgrees(List<PartialResult> results) {
        List<CipherText> cipherTexts = results.stream().map(PartialResult::getCipherText).collect(Collectors.toList());
        List<Integer> voteCounts = results.stream().map(PartialResult::getVotes).collect(Collectors.toList());

        boolean ciphertextEquals = cipherTexts.stream().allMatch(cipherTexts.get(0)::equals);
        boolean voteCountsEqual = voteCounts.stream().allMatch(voteCounts.get(0)::equals);

        return ciphertextEquals && voteCountsEqual;
    }

    /**
     * The Result fetcher Configuration.
     * <br/>
     * Created in the {@link ClientConfigBuilder}.
     */
    public static class ResultFetcherConfiguration extends ClientConfiguration {
        private boolean forceCalculations;

        /**
         * @param targetUrl         url for {@link dk.mmj.evhe.server.bulletinboard.BulletinBoard} to get data from
         * @param forceCalculations whether ciphertext containing sum of votes should be computed locally
         */
        ResultFetcherConfiguration(String targetUrl, boolean forceCalculations) {
            super(targetUrl);
            this.forceCalculations = forceCalculations;
        }
    }
}
