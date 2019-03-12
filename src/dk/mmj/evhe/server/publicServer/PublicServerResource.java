package dk.mmj.evhe.server.publicServer;


import dk.mmj.evhe.crypto.PublicKey;
import dk.mmj.evhe.server.ServerState;
import dk.mmj.evhe.server.VoteDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static dk.mmj.evhe.server.publicServer.PublicServer.*;

@Path("/")
public class PublicServerResource {
    private static Logger logger = LogManager.getLogger(PublicServerResource.class);
    private ServerState state = ServerState.getInstance();

    @GET
    @Path("type")
    @Produces(MediaType.TEXT_HTML)
    public String getType() {
        logger.info("Received request for server type");
        return "<b>ServerType:</b> Public Server";
    }

    @GET
    @Path("publicKey")
    @Produces(MediaType.APPLICATION_JSON)
    public PublicKey getPublicKey() {
        PublicKey publicKey = state.get(PUBLIC_KEY, PublicKey.class);

        if (publicKey == null) {
            NotFoundException notFoundException = new NotFoundException("Currently the server has no public key");
            logger.warn("A request was made for a public key but none was found", notFoundException);
            throw notFoundException;
        }

        return publicKey;
    }

    @POST
    @Path("vote")
    @Consumes(MediaType.APPLICATION_JSON)
    @SuppressWarnings("unchecked")
    public void Vote(VoteDTO vote) {
        Set hasVoted = state.get(HAS_VOTED, HashSet.class);
        String voterId = vote.getId();

        if (hasVoted.contains(voterId)) {
            NotAllowedException e = new NotAllowedException("A vote has already been registered with this ID");
            logger.warn("Voter with id=" + voterId + " attempted to vote more than once", e);
            throw e;
        }

        List votes = state.get(VOTES, ArrayList.class);
        votes.add(vote.getCipherText());
        hasVoted.add(voterId);
    }

    @POST
    @Path("terminate")
    public void terminate() {
        PublicServer server = state.get(SERVER, PublicServer.class);
        server.terminateVoting();
    }
}
