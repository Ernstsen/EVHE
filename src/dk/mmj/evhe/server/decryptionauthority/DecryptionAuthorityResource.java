package dk.mmj.evhe.server.decryptionauthority;

import dk.mmj.evhe.server.ServerState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import static dk.mmj.evhe.server.decryptionauthority.DecryptionAuthority.SERVER;

@Path("/")
public class DecryptionAuthorityResource {
    private static Logger logger = LogManager.getLogger(DecryptionAuthorityResource.class);
    private ServerState state = ServerState.getInstance();

    @GET
    @Path("type")//TODO: REMOVE
    @Produces(MediaType.TEXT_HTML)
    public String test() {
        logger.info("Received request for server type");

        return "<b>ServerType:</b> Key Server";
    }

    @POST
    @Path("terminate")
    public void terminate() {
        DecryptionAuthority server = state.get(SERVER, DecryptionAuthority.class);
        logger.info("Terminating voting");
        server.terminateVoting();
    }
}