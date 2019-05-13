package dk.mmj.evhe.server.decryptionauthority;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
public class DecryptionAuthorityResource {
    private static Logger logger = LogManager.getLogger(DecryptionAuthorityResource.class);

    @GET
    @Path("type")
    @Produces(MediaType.TEXT_HTML)
    public String test() {
        logger.info("Received request for server type");

        return "<b>ServerType:</b> Decryption Authority";
    }
}