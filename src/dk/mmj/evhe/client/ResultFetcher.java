package dk.mmj.evhe.client;

import dk.mmj.evhe.crypto.ElGamal;
import dk.mmj.evhe.crypto.SecurityUtils;
import dk.mmj.evhe.crypto.exceptions.UnableToDecryptException;
import dk.mmj.evhe.crypto.zeroknowledge.VoteProofUtils;
import dk.mmj.evhe.entities.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        PublicInformationEntity publicInformationEntity = fetchPublicInfo();
        long endTime = publicInformationEntity.getEndTime();
        if (new Date().getTime() < endTime) {
            long diff = (endTime - new Date().getTime()) / 60_000;
            logger.info("The vote has not yet terminated, so results are unavailable. " +
                    "The vote should terminate in about " + diff + " minutes");
            return;
        }

        PublicKey publicKey = getPublicKey();
        ResultList resultList = target.path("result").request().get(ResultList.class);

        List<PartialResult> results = resultList.getResults();

        Map<Integer, BigInteger> partials = new HashMap<>();

        logger.info("Combining partials to total result");
        for (Object result : results) {
            if (!(result instanceof PartialResult)) {
                logger.error("A partial result were not instanceof PartialResult. Was: " + result.getClass() + ". Terminating");
                System.exit(-1);
            }
            PartialResult res = (PartialResult) result;
            partials.put(res.getId(), res.getResult());
        }


        logger.info("Fetching votes");
        VoteList votes = target.path("getVotes").request().get(VoteList.class);
        logger.debug("Filtering votes");
        long totalVotes = votes.getVotes().stream()
                .filter(v -> VoteProofUtils.verifyProof(v, publicKey))
//                .filter(v -> v.getTs().getTime() < endTime) //TODO: FIX
                .count();


        int result = 0;
        try {
            CipherText acc = new CipherText(BigInteger.ONE, BigInteger.ONE);
            CipherText sum = votes.getVotes().stream()
                    .filter(v -> VoteProofUtils.verifyProof(v, publicKey))
                    .map(VoteDTO::getCipherText)
                    .reduce(acc, ElGamal::homomorphicAddition);

            BigInteger c = SecurityUtils.combinePartials(partials, publicKey.getP());
            result = ElGamal.homomorphicDecryptionFromPartials(sum, c, publicKey.getG(), publicKey.getP(), Integer.MAX_VALUE);
        } catch (UnableToDecryptException e) {
            logger.error("Failed to decrypt from partial decryptions", e);
        }

        logger.info("Result: " + result + "/" + totalVotes);
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
