package dk.mmj.evhe.initialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.eSoftware.commandLineParser.Configuration;
import dk.mmj.evhe.Application;
import dk.mmj.evhe.crypto.ElGamal;
import dk.mmj.evhe.crypto.SecurityUtils;
import dk.mmj.evhe.crypto.keygeneration.KeyGenerationParametersImpl;
import dk.mmj.evhe.entities.DistKeyGenResult;
import dk.mmj.evhe.entities.PublicInformationEntity;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.signers.RSADigestSigner;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.util.encoders.Base64;
import org.glassfish.jersey.client.JerseyWebTarget;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.security.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static dk.mmj.evhe.client.SSLHelper.configureWebTarget;

public class TrustedDealer implements Application {
    private static final String PRIVATE_KEY_NAME = "rsa.pub";
    private static final String PUBLIC_KEY_NAME = "rsa";
    private static final Logger logger = LogManager.getLogger(TrustedDealer.class);
    private JerseyWebTarget bulletinBoard;
    private int polynomialDegree;
    private int servers;
    private long endTime;
    private Path rootPath;
    private Path keyPath;

    public TrustedDealer(TrustedDealerConfiguration config) {
        bulletinBoard = configureWebTarget(logger, config.bulletinBoardPath);
        this.polynomialDegree = config.polynomialDegree;
        this.servers = config.servers;
        this.rootPath = config.rootPath;
        this.keyPath = config.keyPath;
        this.endTime = config.endTime;

        createIfNotExists(rootPath);
        createIfNotExists(keyPath);

        if (config.newKey) {
            try {
                KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA", "BC");
                gen.initialize(2048, new SecureRandom());

                KeyPair keyPair = gen.generateKeyPair();
                PublicKey pk = keyPair.getPublic();
                PrivateKey sk = keyPair.getPrivate();

                writeFile(keyPath.resolve(PUBLIC_KEY_NAME).toFile(), Base64.encode(pk.getEncoded()));
                writeFile(keyPath.resolve(PRIVATE_KEY_NAME).toFile(), Base64.encode(sk.getEncoded()));
            } catch (NoSuchAlgorithmException e) {
                logger.error("Unable to find RSA KeyGen algorithm. Terminating", e);
                System.exit(-1);
            } catch (NoSuchProviderException e) {
                logger.error("Unable to find bouncycastle provider. Terminating", e);
                System.exit(-1);
            }
        }
    }


    @Override
    public void run() {
        logger.info("Starting key generation");
        KeyGenerationParametersImpl params = new KeyGenerationParametersImpl(1024, 50);

        DistKeyGenResult distKeyGenResult = ElGamal.generateDistributedKeys(params, polynomialDegree, servers);

        logger.info("Compiling key information to files");

        Map<Integer, BigInteger> secretValues = distKeyGenResult.getSecretValues();
        Map<Integer, BigInteger> publicValues = distKeyGenResult.getPublicValues();

        List<String> output = new ArrayList<>();

        BigInteger h = SecurityUtils.combinePartials(publicValues, distKeyGenResult.getP());

        dk.mmj.evhe.entities.PublicKey publicKey = new dk.mmj.evhe.entities.PublicKey(h, distKeyGenResult.getG(), distKeyGenResult.getQ());

        ObjectMapper mapper = new ObjectMapper();

        String publicKeyString;
        try {
            publicKeyString = mapper.writeValueAsString(publicKey);
        } catch (JsonProcessingException e) {
            logger.error("Failed to write public key as String. Terminating", e);
            System.exit(-1);
            return;
        }

        distKeyGenResult.getAuthorityIds().stream()
                .map(id -> id + "\n" +
                        secretValues.get(id) + "\n" +
                        distKeyGenResult.getP() + "\n" +
                        publicKeyString + "\n" +
                        endTime + "\n")
                .forEach(output::add);

        logger.info("Writing files");
        for (int i = 0; i < output.size(); i++) {
            File dest = rootPath.resolve(Integer.toString(i + 1)).toFile();
            writeFile(dest, output.get(i).getBytes());
        }

        PublicInformationEntity publicInformation = new PublicInformationEntity(
                distKeyGenResult.getAuthorityIds(),
                publicValues,
                distKeyGenResult.getG(),
                distKeyGenResult.getQ(),
                distKeyGenResult.getP(),
                endTime);

        logger.info("Signing public information");
        File privateFile = keyPath.resolve(PRIVATE_KEY_NAME).toFile();
        try {
            AsymmetricKeyParameter privateKey = loadKey(privateFile);
            RSADigestSigner signer = new RSADigestSigner(new SHA256Digest());
            signer.init(true, privateKey);
            publicInformation.updateSigner(signer);
            byte[] sigArray = signer.generateSignature();
            String signature = new String(Base64.encode(sigArray));
            publicInformation.setSignature(signature);
        } catch (CryptoException e) {
            logger.error("Failed to create RSA signature", e);
        }

        logger.info("Posting to Bulletin Board");
        post(publicInformation);
        logger.info("Finished.");

    }

    private AsymmetricKeyParameter loadKey(File keyFile) {
        try {
            byte[] bytes = new byte[2048];
            int len = IOUtils.readFully(new FileInputStream(keyFile), bytes);
            byte[] actualBytes = Arrays.copyOfRange(bytes, 0, len);
            return PrivateKeyFactory.createKey(Base64.decode(actualBytes));
        } catch (IOException e) {
            logger.error("Unable to read privateKey from file. Terminating", e);
            System.exit(-1);
        }

        return null;
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

    private void createIfNotExists(Path path) {
        File file = path.toFile();

        if (file.exists()) {
            return;
        }

        boolean mkdirs = file.mkdirs();
        if (!mkdirs) {
            logger.warn("Unable to create dir " + path.toAbsolutePath().toString());
        }
    }

    private void writeFile(File dest, byte[] value) {
        try (FileOutputStream ous = new FileOutputStream(dest)) {
            ous.write(value);
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
        private boolean newKey;
        private long endTime;

        /**
         * Constructor for the Trusted Dealer configuration
         *
         * @param rootPath          root path used when writing output files
         * @param keyPath           path to the private key used for signing values
         * @param servers           number of servers to create files for
         * @param polynomialDegree  the degree of the polynomial used during key generation
         * @param bulletinBoardPath path to the bulletin board where public key should be posted
         * @param newKey            whether new key should be generated in the root
         * @param endTime           When the vote comes to an end. ms since January 1, 1970, 00:00:00 GMT
         */
        TrustedDealerConfiguration(Path rootPath, Path keyPath, int servers, int polynomialDegree, String bulletinBoardPath, boolean newKey, long endTime) {
            this.rootPath = rootPath;
            this.keyPath = keyPath;
            this.servers = servers;
            this.polynomialDegree = polynomialDegree;
            this.bulletinBoardPath = bulletinBoardPath;
            this.newKey = newKey;
            this.endTime = endTime;
        }
    }
}
