package dk.mmj.evhe.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.eSoftware.commandLineParser.Configuration;
import dk.mmj.evhe.Application;
import dk.mmj.evhe.crypto.SecurityUtils;
import dk.mmj.evhe.entities.*;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.signers.RSADigestSigner;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.util.encoders.Base64;
import org.glassfish.jersey.client.JerseyWebTarget;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Paths;
import java.util.*;

import static dk.mmj.evhe.client.SSLHelper.configureWebTarget;

public abstract class Client implements Application {
    private static final String PUBLIC_KEY_NAME = "rsa";
    JerseyWebTarget target;
    private Logger logger = LogManager.getLogger(Client.class);
    private PublicInformationEntity publicInfo;

    public Client(ClientConfiguration configuration) {
        List<Class> classes = Arrays.asList(
                HashMap.class,
                Map.class,
                VoteDTO.class,
                PublicKey.class,
                CipherText.class,
                VoteDTO.Proof.class,
                PublicInfoList.class,
                PublicInformationEntity.class);

        target = configureWebTarget(logger, configuration.targetUrl, classes);
    }

    /**
     * Fetches the public key by requesting it from the public servers "/publicKey" path.
     *
     * @return the response containing the Public Key.
     */
    protected PublicKey getPublicKey() {

        PublicInformationEntity info = fetchPublicInfo();

        BigInteger h = SecurityUtils.combinePartials(info.getPublicKeys(), info.getP());

        return new PublicKey(h, info.getG(), info.getQ());
    }

    protected PublicInformationEntity fetchPublicInfo() {
        if (publicInfo != null) {
            return publicInfo;
        }

        Response response = target.path("getPublicInfo").request().buildGet().invoke();
        String responseString = response.readEntity(String.class);

        PublicInfoList publicInfoList;
        try {
            publicInfoList = new ObjectMapper().readerFor(PublicInfoList.class).readValue(responseString);
        } catch (IOException e) {
            logger.error("Failed to deserialize public informations list retrieved from bulletin board", e);
            System.exit(-1);
            return null;//Never happens
        }

        Optional<PublicInformationEntity> any = publicInfoList.getInformationEntities().stream()
                .filter(this::verifyPublicInformation)
                .findAny();

        if (!any.isPresent()) {
            logger.error("No public information retrieved from the server was signed by the trusted dealer. Terminating");
            System.exit(-1);
            return null;//Never happens
        }
        publicInfo = any.get();
        return publicInfo;
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

    public static class ClientConfiguration implements Configuration {
        private final String targetUrl;

        ClientConfiguration(String targetUrl) {
            this.targetUrl = targetUrl;
        }
    }
}
