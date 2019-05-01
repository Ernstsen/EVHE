package dk.mmj.evhe.client;

import dk.mmj.evhe.entities.PublicKey;

import java.util.List;

public class ResultFetcher extends Client {

    public ResultFetcher(ResultFetcherConfiguration configuration) {
        super(configuration);
    }

    @Override
    public void run() {
        PublicKey publicKey = getPublicKey();
        List result = target.path("result").request().get(List.class);

        //TODO: Combine results
        //TODO: Fetch votes, filter them, get ciphertext.
        //TODO: Log stuff, including result.
    }

    /**
     * The Result fetcher Configuration.
     * <br/>
     * Created in the {@link ClientConfigBuilder}.
     */
    public static class ResultFetcherConfiguration extends ClientConfiguration {

        ResultFetcherConfiguration(String targetUrl, String id) {
            super(targetUrl);
        }
    }
}
