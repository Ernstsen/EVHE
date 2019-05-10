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

    public ResultFetcher(ResultFetcherConfiguration configuration) {
        super(configuration);
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
        if (results == null) {
            logger.info("Did not fetch any results. Probable cause is unfinished decryption. Try again later");
            return;
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


        CipherText sum = SecurityUtils.concurrentVoteSum(votes.getVotes(), publicKey, 1000);
        BigInteger d = sum.getD();


        Map<Integer, BigInteger> partials = new HashMap<>();
        logger.info("Combining partials to total result");
        for (Object result : results) {
            if (!(result instanceof PartialResult)) {
                logger.error("A partial result were not instanceof PartialResult. Was: " + result.getClass() + ". Terminating");
                System.exit(-1);
            }
            PartialResult res = (PartialResult) result;

            CipherText partialDecryption = new CipherText(res.getResult(), d);
            PublicKey partialPublicKey = new PublicKey(
                    publicInformationEntity.getPublicKeys().get(res.getId()),
                    publicInformationEntity.getG(),
                    publicInformationEntity.getQ());
            boolean validProof = DLogProofUtils.verifyProof(sum, partialDecryption, partialPublicKey, res.getProof(), res.getId());

            if (validProof) {
                partials.put(res.getId(), res.getResult());
            }
        }

        int result = 0;
        try {
            logger.info("Summing votes and decrypting from partials");
            BigInteger cs = SecurityUtils.combinePartials(partials, publicKey.getP());
            result = ElGamal.homomorphicDecryptionFromPartials(d, cs, publicKey.getG(), publicKey.getP(), actualVotes.size());
        } catch (UnableToDecryptException e) {
            logger.error("Failed to decrypt from partial decryptions. Unable to server result", e);
            System.exit(-1);
        }

        logger.info("Result: " + result + "/" + actualVotes.size());
    }

    /**
     * The Result fetcher Configuration.
     * <br/>
     * Created in the {@link ClientConfigBuilder}.
     */
    public static class ResultFetcherConfiguration extends ClientConfiguration {

        ResultFetcherConfiguration(String targetUrl) {
            super(targetUrl);
        }
    }
}
