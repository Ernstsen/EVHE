package dk.mmj.evhe.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.mmj.evhe.crypto.ElGamal;
import dk.mmj.evhe.crypto.SecurityUtils;
import dk.mmj.evhe.crypto.exceptions.UnableToDecryptException;
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
        if(results == null){
            logger.info("Did not fetch any results. Probable cause is unfinished decryption. Try again later");
            return;
        }


        Map<Integer, BigInteger> partials = new HashMap<>();
        Map<BigInteger, Integer> dValues = new HashMap<>();
        logger.info("Combining partials to total result");
        for (Object result : results) {
            if (!(result instanceof PartialResult)) {
                logger.error("A partial result were not instanceof PartialResult. Was: " + result.getClass() + ". Terminating");
                System.exit(-1);
            }
            PartialResult res = (PartialResult) result;
            partials.put(res.getId(), res.getResult());

            Integer currCnt = dValues.get(res.getD());
            dValues.put(res.getD(), currCnt == null ? 1 : currCnt + 1);
        }

        BigInteger d = getMaxValueKey(dValues);

        logger.info("Fetching votes");
        VoteList votes;
        List<PersistedVote> actualVotes;
        try {
            String getVotes = target.path("getVotes").request().get(String.class);
            votes = new ObjectMapper().readerFor(VoteList.class).readValue(getVotes);
            logger.debug("Filtering votes");
            actualVotes = votes.getVotes().stream()
                    .filter(v -> v.getTs().getTime() < endTime)
                    .filter(v -> VoteProofUtils.verifyProof(v, publicKey))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("Failed to read votes from server", e);
            System.exit(-1);
            return;
        }

        if (d == null) {
            logger.warn("No valid d value found. Calculating own");
            CipherText acc = new CipherText(BigInteger.ONE, BigInteger.ONE);

            CipherText sum = votes.getVotes().stream()
                    .filter(v -> VoteProofUtils.verifyProof(v, publicKey))
                    .map(VoteDTO::getCipherText)
                    .reduce(acc, ElGamal::homomorphicAddition);
            d = sum.getD();
        }

        int result = 0;
        try {
            logger.info("Summing votes and decrypting from partials");
            BigInteger c = SecurityUtils.combinePartials(partials, publicKey.getP());
            result = ElGamal.homomorphicDecryptionFromPartials(d, c, publicKey.getG(), publicKey.getP(), actualVotes.size());
        } catch (UnableToDecryptException e) {
            logger.error("Failed to decrypt from partial decryptions. Unable to server result", e);
            System.exit(-1);
        }

        logger.info("Result: " + result + "/" + actualVotes.size());
    }

    private BigInteger getMaxValueKey(Map<BigInteger, Integer> dValues) {
        BigInteger d = null;
        int maxOcc = 0;
        for (Map.Entry<BigInteger, Integer> entry : dValues.entrySet()) {
            if (entry.getValue() > maxOcc) {
                d = entry.getKey();
            }
        }
        return d;
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
