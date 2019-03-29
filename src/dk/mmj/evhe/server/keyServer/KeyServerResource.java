package dk.mmj.evhe.server.keyServer;

import dk.mmj.evhe.crypto.*;
import dk.mmj.evhe.crypto.entities.CipherText;
import dk.mmj.evhe.crypto.entities.KeyPair;
import dk.mmj.evhe.crypto.entities.PublicKey;
import dk.mmj.evhe.crypto.exceptions.UnableToDecryptException;
import dk.mmj.evhe.server.ServerState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.math.BigInteger;

import static dk.mmj.evhe.server.keyServer.KeyServer.KEY_PAIR;

@Path("/")
public class KeyServerResource {
    private static Logger logger = LogManager.getLogger(KeyServerResource.class);
    private ServerState state = ServerState.getInstance();

    @GET
    @Path("type")
    @Produces(MediaType.TEXT_HTML)
    public String test() {
        logger.info("Received request for server type");

        return "<b>ServerType:</b> Key Server";
    }

    @GET
    @Path("publicKey")
    public PublicKey getPublicKey() {
        KeyPair keyPair = state.get(KEY_PAIR, KeyPair.class);

        return keyPair.getPublicKey();
    }

    @POST
    @Path("result")
    public BigInteger calculateResult(CipherText cipherText) throws UnableToDecryptException {
        KeyPair keyPair = state.get(KEY_PAIR, KeyPair.class);
        int result = ElGamal.homomorphicDecryption(keyPair, cipherText, 1000);

        return BigInteger.valueOf(result);
    }
}