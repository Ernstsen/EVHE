package dk.mmj.evhe.client;

import dk.eSoftware.commandLineParser.Configuration;
import dk.mmj.evhe.Application;
import dk.mmj.evhe.crypto.SecurityUtils;
import dk.mmj.evhe.entities.*;
import dk.mmj.evhe.server.decryptionauthority.DecryptionAuthorityConfigBuilder;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.signers.RSADigestSigner;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.util.encoders.Base64;
import org.glassfish.jersey.client.JerseyWebTarget;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.math.BigInteger;
import java.nio.file.Paths;
import java.util.*;

import static dk.mmj.evhe.client.SSLHelper.configureWebTarget;

public class Client implements Application {
    private static final Logger logger = LogManager.getLogger(DecryptionAuthorityConfigBuilder.class);
    private JerseyWebTarget target;
    private String id;
    private Boolean vote;
    private Integer multi;
    private static final String PUBLIC_KEY_NAME = "rsa";

    /**
     * Creates a client instance, that utilizes the SSL protocol to communicate with the public server.
     *
     * @param configuration the ClientConfiguration built in the same class.
     */
    public Client(ClientConfiguration configuration) {

        id = configuration.id;
        vote = configuration.vote;
        multi = configuration.multi;

        List<Class> classes = Arrays.asList(
                HashMap.class,
                VoteDTO.class,
                PublicKey.class,
                CipherText.class,
                VoteDTO.Proof.class,
                PublicInfoList.class,
                PublicInformationEntity.class);

        target = configureWebTarget(logger, configuration.targetUrl, classes);
    }


    /**
     * Fetches the public key from the public server, and casts vote.
     * <br/>
     * if <code>multi</code> is set it casts <code>multi</code> random votes, for testing purposes.
     * <br/>
     * Otherwise just casts a single, specified vote.
     */
    @Override
    public void run() {
        assertBulletinBoard();

        PublicKey publicKey = getPublicKey();
        if (multi != null) {
            doMultiVote(publicKey);
        } else {
            int vote = getVote();
            doVote(vote, publicKey);
        }
    }

    /**
     * Casts <code>multi</code> random votes, for testing purposes.
     *
     * @param publicKey is the public key used to encrypt the vote.
     */
    private void doMultiVote(PublicKey publicKey) {
        Random random = new Random();
        int trueVotes = 0;
        int falseVotes = 0;

        for (int i = 0; i < multi; i++) {
            System.out.print("Dispatching votes: " + i + "/" + multi + " \r");

            id = UUID.randomUUID().toString();
            int vote = random.nextInt(2);

            if (vote == 0) {
                falseVotes++;
            } else {
                trueVotes++;
            }

            doVote(vote, publicKey);
        }

        System.out.println("Dispatched " + multi + " votes with " + trueVotes + " for, and " + falseVotes + " against");
    }

    /**
     * Encrypts the vote under the public key, and casts the encrypted vote.
     *
     * @param publicKey is the public key used to encrypt the vote.
     * @param vote      is the vote to be cast, either 0 or 1.
     */
    private void doVote(int vote, PublicKey publicKey) {
        VoteDTO voteDTO = SecurityUtils.generateVote(vote, id, publicKey);
        postVote(voteDTO);
    }

    /**
     * Makes sure that the {@link javax.ws.rs.client.WebTarget} is a public-server, and that it is live.
     * <br/>
     * Throws a {@link RuntimeException} if this is not the case.
     */
    private void assertBulletinBoard() {
        Response publicServerResp = target.path("type").request().buildGet().invoke();

        if (publicServerResp.getStatus() != 200) {
            logger.error("Couldn't connect to the bulletinBoard.");
            throw new RuntimeException("Failed : HTTP error code : " + publicServerResp.getStatus());
        }

        String responseEntity = publicServerResp.readEntity(String.class);

        if (!responseEntity.contains("Bulletin Board")) {
            throw new RuntimeException("Server was not of type bulletinBoard");
        }
    }

    /**
     * Posts the encrypted vote to the public server, using the "/vote" path.
     *
     * @param vote the VoteDTO with vote encrypted under the public key, and zero knowledge proof.
     */
    private void postVote(VoteDTO vote) {
        Entity<?> entity = Entity.entity(vote, MediaType.APPLICATION_JSON_TYPE);
        Response response = target.path("vote").request().post(entity);

        if (response.getStatus() != 204) {
            logger.warn("Failed to post vote to server: Error code was " + response.getStatus());
        }
    }

    /**
     * Retrieves vote to be cast
     * <br/>
     * If none is defined, terminal is prompted for one
     * <br/>
     * Valid inputs for the vote is either "true" or "false".
     *
     * @return 1 or 0 according to input.
     */
    private int getVote() {
        if (vote == null) {
            System.out.println("Please enter vote to be cast: true/false");
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            try {
                String s = reader.readLine();
                vote = Boolean.parseBoolean(s);
                System.out.println("voting: " + vote);
            } catch (IOException ignored) {
                System.out.println("Unable to read vote - terminating");
                System.exit(-1);
            }
        }

        if (vote) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * Fetches the public key by requesting it from the public servers "/publicKey" path.
     *
     * @return the response containing the Public Key.
     */
    private PublicKey getPublicKey() {
        Response response = target.path("getPublicInfo").request().buildGet().invoke();
        PublicInfoList publicInfoList = response.readEntity(PublicInfoList.class);

        Optional<PublicInformationEntity> any = publicInfoList.getInformationEntities().stream()
                .filter(this::verifyPublicInformation)
                .findAny();

        if (!any.isPresent()) {
            logger.error("No public information retrieved from the server was signed by the trusted dealer. Terminating");
            System.exit(-1);
            return null;//Never happens
        }

        PublicInformationEntity info = any.get();

        BigInteger h = SecurityUtils.combinePartials(info.getPublicKeys(), info.getP());

        return new PublicKey(h, info.getG(), info.getQ());
    }

    private boolean verifyPublicInformation(PublicInformationEntity info) {
        File keyFile = Paths.get("rsa").resolve(PUBLIC_KEY_NAME).toFile();
        if (!keyFile.exists()) {
            logger.error("Unable to locate RSA public key from TD");
            return false;
        }

        try {
            byte[] bytes = new byte[2048];
            int len = IOUtils.readFully(new FileInputStream(keyFile), bytes);
            byte[] actualBytes = Arrays.copyOfRange(bytes, 0, len);

            AsymmetricKeyParameter key = PublicKeyFactory.createKey(Base64.decode(actualBytes));
            RSADigestSigner dig = new RSADigestSigner(new SHA256Digest());
            dig.init(false, key);
            info.updateSigner(dig);

            byte[] encodedSignature = info.getSignature().getBytes();
            return dig.verifySignature(Base64.decode(encodedSignature));
        } catch (IOException e) {
            logger.error("Failed to verify signature", e);
            return false;
        }
    }

    /**
     * The Client Configuration.
     * <br/>
     * Created in the {@link ClientConfigBuilder}.
     */
    public static class ClientConfiguration implements Configuration {
        private final String targetUrl;
        private final String id;
        private final Boolean vote;
        private final Integer multi;

        ClientConfiguration(String targetUrl, String id, Boolean vote, Integer multi) {
            this.targetUrl = targetUrl;
            this.id = id;
            this.vote = vote;
            this.multi = multi;
        }
    }
}
