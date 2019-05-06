package dk.mmj.evhe.client;

import dk.mmj.evhe.crypto.SecurityUtils;
import dk.mmj.evhe.crypto.zeroknowledge.VoteProofUtils;
import dk.mmj.evhe.entities.PartialResult;
import dk.mmj.evhe.entities.PublicInformationEntity;
import dk.mmj.evhe.entities.PublicKey;
import dk.mmj.evhe.entities.VoteList;
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
            long diff = (new Date().getTime() - endTime) / 60_000;
            logger.info("The vote has not yet terminated, so results are unavailable. " +
                    "The vote should terminate in about " + diff + " minutes");
        }

        PublicKey publicKey = getPublicKey();
        List results = target.path("result").request().get(List.class);

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
        BigInteger result = SecurityUtils.combinePartials(partials, publicKey.getP());

        logger.info("Fetching votes for total vote count");
        VoteList votes = target.path("getVotes").request().get(VoteList.class);
        logger.debug("Filtering votes");
        long totalVotes = votes.getVotes().stream()
                .filter(v -> VoteProofUtils.verifyProof(v, publicKey))
                .filter(v -> v.getTs().getTime() < endTime)
                .count();


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
