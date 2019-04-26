package dk.mmj.evhe.initialization;

import dk.eSoftware.commandLineParser.Configuration;
import dk.mmj.evhe.Application;
import dk.mmj.evhe.crypto.ElGamal;
import dk.mmj.evhe.crypto.entities.DistKeyGenResult;
import dk.mmj.evhe.crypto.keygeneration.KeyGenerationParametersImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.JerseyWebTarget;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
    private Path rootPath;

    public TrustedDealer(TrustedDealerConfiguration config) {
        bulletinBoard = configureWebTarget(logger, config.bulletinBoardPath, new ArrayList<>());
        this.polynomialDegree = config.polynomialDegree;
        this.servers = config.servers;
        this.rootPath = config.rootPath;
    }


    @Override
    public void run() {
        KeyGenerationParametersImpl params = new KeyGenerationParametersImpl(1024, 50);

        DistKeyGenResult distKeyGenResult = ElGamal.generateDistributedKeys(params, polynomialDegree, servers);

        Map<Integer, BigInteger> secretValues = distKeyGenResult.getSecretValues();
        Map<Integer, BigInteger> publicValues = distKeyGenResult.getPublicValues();

        List<String> output = new ArrayList<>();

        distKeyGenResult.getAuthorityIds().stream()
                .map(id -> id + "\n" +
                        publicValues.get(id) + "\n" +
                        secretValues.get(id) + "\n" +
                        distKeyGenResult.getG() + "\n" +
                        distKeyGenResult.getQ() + "\n" +
                        distKeyGenResult.getP() + "\n")
                .forEach(output::add);

        for (int i = 0; i < output.size(); i++) {
            File dest = rootPath.resolve(Integer.toString(i)).toFile();
            writeOutput(dest, output.get(i));
        }

        PublicInformationEntity publicInformation = new PublicInformationEntity(
                distKeyGenResult.getAuthorityIds(),
                publicValues,
                distKeyGenResult.getG(),
                distKeyGenResult.getQ(),
                distKeyGenResult.getP());

        //TODO: SIGN

        Entity<PublicInformationEntity> entity = Entity.entity(publicInformation, MediaType.APPLICATION_JSON);
        Response response = bulletinBoard.request().post(entity);
        if (response.getStatus() <= 200 || response.getStatus() >= 300) {
            logger.error("Unable to post information to bulletin board, response code was " + response.getStatus());
        }

    }

    private void writeOutput(File dest, String value) {
        dest.mkdirs();
        try (FileOutputStream ous = new FileOutputStream(dest)) {
            ous.write(value.getBytes());
            ous.flush();
        } catch (IOException e) {
            logger.error("Failed to write to file: " + dest.getAbsolutePath(), e);
        }
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
