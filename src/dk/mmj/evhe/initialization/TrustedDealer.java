package dk.mmj.evhe.initialization;

import dk.eSoftware.commandLineParser.Configuration;
import dk.mmj.evhe.Application;

import java.nio.file.Path;

public class TrustedDealer implements Configuration, Application {

    @Override
    public void run() {

    }


    /**
     * Configuration class for the trusted dealer
     */
    static class TrustedDealerConfiguration implements Configuration {

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
