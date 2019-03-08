package dk.mmj.evhe.server.publicServer;


import dk.mmj.evhe.crypto.PublicKey;
import dk.mmj.evhe.server.ServerState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
public class PublicServerResource {
    private static Logger logger = LogManager.getLogger(PublicServerResource.class);

    @GET
    @Path("type")
    @Produces(MediaType.TEXT_HTML)
    public String getType() {
        logger.info("Received request for server type");
        return "<b>ServerType:</b> Public Server";
    }

    @GET
    @Path("publicKey")
    @Produces(MediaType.WILDCARD)
    public PublicKey getPublicKey() {
        PublicKey publicKey = ServerState.getInstance().get("publicKey", PublicKey.class);
        if (publicKey == null) {
            NotFoundException notFoundException = new NotFoundException("Currently the server has no public key");
            logger.warn("A request was made for a public key but none was found", notFoundException);
            throw notFoundException;
        }

        return publicKey;
    }
}
