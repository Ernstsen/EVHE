package dk.mmj.evhe.initialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.eSoftware.commandLineParser.Configuration;
import dk.mmj.evhe.Application;
import dk.mmj.evhe.crypto.ElGamal;
import dk.mmj.evhe.entities.DistKeyGenResult;
import dk.mmj.evhe.crypto.keygeneration.KeyGenerationParametersImpl;
import dk.mmj.evhe.entities.PublicInformationEntity;
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
import java.util.*;

import static dk.mmj.evhe.client.SSLHelper.configureWebTarget;

public class TrustedDealer implements Configuration, Application {
    private static Logger logger = LogManager.getLogger(TrustedDealer.class);
    private JerseyWebTarget bulletinBoard;
    private int polynomialDegree;
    private int servers;
    private Path rootPath;

    public TrustedDealer(TrustedDealerConfiguration config) {
        bulletinBoard = configureWebTarget(logger, config.bulletinBoardPath, Arrays.asList(
                PublicInformationEntity.class,
                Map.class,
                HashMap.class,
                List.class,
                ArrayList.class
        ));
        this.polynomialDegree = config.polynomialDegree;
        this.servers = config.servers;
        this.rootPath = config.rootPath;
    }


    @Override
    public void run() {
        logger.info("Beginning Keygeneration");
        KeyGenerationParametersImpl params = new KeyGenerationParametersImpl(1024, 50);

        DistKeyGenResult distKeyGenResult = ElGamal.generateDistributedKeys(params, polynomialDegree, servers);

        logger.info("Compiling key information to files");

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

        logger.info("Asserting existence of root dir");
        createIfNotExists();

        logger.info("Writing files");
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

        logger.info("Posting to Bulletin Board");
        post(publicInformation);
        logger.info("Finished.");

    }

    private void post(PublicInformationEntity publicInformation) {

        try {
            Entity entity = Entity.entity(new ObjectMapper().writeValueAsString(publicInformation), MediaType.APPLICATION_JSON);
            Response response = bulletinBoard.path("postPublicInfo").request().post(entity);
            if (response.getStatus() <= 200 || response.getStatus() >= 300) {
                logger.error("Unable to post information to bulletin board, response code was " + response.getStatus());
            }
        } catch (JsonProcessingException e) {
            logger.error("Unable to serialize");
        }
    }

    private void createIfNotExists() {
        boolean mkdirs = rootPath.toFile().mkdirs();
        if (!mkdirs) {
            logger.warn("Unable to create dir " + rootPath.toAbsolutePath().toString());
        }
    }

    private void writeOutput(File dest, String value) {
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
