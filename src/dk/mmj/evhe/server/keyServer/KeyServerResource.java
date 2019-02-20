package dk.mmj.evhe.server.keyServer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
public class KeyServerResource {
    private static Logger logger = LogManager.getLogger(KeyServerResource.class);

    @GET
    @Path("type")
    @Produces(MediaType.TEXT_HTML)
    public String test() {
        logger.info("Received request for server type");
        return "<b>ServerType:</b> Key Server";
    }
}
