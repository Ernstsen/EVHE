package dk.mmj.evhe.initialization;

import dk.eSoftware.commandLineParser.Configuration;
import dk.mmj.evhe.Application;
import dk.mmj.evhe.crypto.SecurityUtils;
import dk.mmj.evhe.crypto.entities.PrimePair;
import dk.mmj.evhe.crypto.keygeneration.KeyGenerationParametersImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.JerseyWebTarget;

import java.math.BigInteger;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static dk.mmj.evhe.client.SSLHelper.configureWebTarget;

public class TrustedDealer implements Configuration, Application {
    private static Logger logger = LogManager.getLogger(TrustedDealer.class);
    private JerseyWebTarget bulletinBoard;
    private int polynomialDegree;
    private int servers;

    public TrustedDealer(TrustedDealerConfiguration config) {
        bulletinBoard = configureWebTarget(logger, config.bulletinBoardPath, new ArrayList<>());
        this.polynomialDegree = config.polynomialDegree;
        this.servers = config.servers;

    }


    @Override
    public void run() {
        KeyGenerationParametersImpl params = new KeyGenerationParametersImpl(1024, 50);
        PrimePair primePair = params.getPrimePair();

        BigInteger[] pol = SecurityUtils.generatePolynomial(polynomialDegree, primePair.getQ());
        Map<Integer, BigInteger> secretValues = SecurityUtils.generateSecretValues(pol, servers);
        Map<Integer, BigInteger> publicValues = SecurityUtils.generatePublicValues(secretValues, params.getGenerator(), primePair.getP());

        List<String> output = new ArrayList<>();

        publicValues.keySet().stream()
                .map(id -> id + "\n" +
                        publicValues.get(id) + "\n" +
                        secretValues.get(id) + "\n" +
                        params.getGenerator() + "\n" +
                        primePair.getQ() + "\n" +
                        primePair.getP() + "\n")
                .forEach(output::add);
        //TODO: Do something with output

    }


    /**
     * Configuration class for the trusted dealer
     */
    public static class TrustedDealerConfiguration implements Configuration {

        private Path rootPath;
        private Path keyPath;
        private int servers;
        private int polynomialDegree;
        private String bulletinBoardPath;

        /**
         * Constructor for the Trusted Dealer configuration
         *
         * @param rootPath          root path used when writing output files
         * @param keyPath           path to the private key used for signing values
         * @param servers           number of servers to create files for
         * @param polynomialDegree  the degree of the polynomial used during key generation
         * @param bulletinBoardPath path to the bulletin board where public key should be posted
         */
        TrustedDealerConfiguration(Path rootPath, Path keyPath, int servers, int polynomialDegree, String bulletinBoardPath) {
            this.rootPath = rootPath;
            this.keyPath = keyPath;
            this.servers = servers;
            this.polynomialDegree = polynomialDegree;
            this.bulletinBoardPath = bulletinBoardPath;
        }
    }
}
