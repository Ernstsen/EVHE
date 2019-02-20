package dk.mmj.evhe.server.publicServer;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.GET;
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
}
