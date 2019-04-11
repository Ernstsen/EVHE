package dk.mmj.evhe.server.bulletinBoard;


import dk.mmj.evhe.crypto.entities.PublicKey;
import dk.mmj.evhe.server.ServerState;
import dk.mmj.evhe.server.VoteDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;

import static dk.mmj.evhe.server.bulletinBoard.BulletinBoard.*;

@Path("/")
public class BulletinBoardResource {
    private static final String ID_LIST = "idList";
    private static Logger logger = LogManager.getLogger(BulletinBoardResource.class);
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
            logger.warn("A request was made for a public key but none was found");
            throw new NotFoundException("Currently the server has no public key");
        }

        return publicKey;
    }

    @POST
    @Path("publicKey")
    @Consumes(MediaType.APPLICATION_JSON)
    public void setPublicKey(PublicKey publicKey) {
        if (publicKey == null) {//TODO: Only once?
            logger.warn("A submitted key CANNOT be null");
            throw new NotAllowedException("Key was null");

        }

        state.put(PUBLIC_KEY, publicKey);
    }

    @POST
    @Path("vote")
    @Consumes(MediaType.APPLICATION_JSON)
    @SuppressWarnings("unchecked")
    public void Vote(VoteDTO vote) {
        if (state.get(RESULT, String.class) != null) {
            logger.warn("A vote as attempted to be cast after voting had been terminated");
            throw new NotAllowedException("Voting has been terminated");
        }

        Set hasVoted = state.get(HAS_VOTED, HashSet.class);
        ArrayList idList = state.get(ID_LIST, ArrayList.class);
        Boolean isTest = state.get(IS_TEST, Boolean.class);
        String voterId = vote.getId();

        if (!isTest && !idList.contains(voterId)) {
            logger.warn("Unrecognized voter with id=" + voterId);
            throw new NotAllowedException("Vote was attempted with unrecognized id=" + voterId);
        }

        if (hasVoted.contains(voterId)) {//TODO: Incompatible with BB?
            logger.warn("Voter with id=" + voterId + " attempted to vote more than once");
            throw new NotAllowedException("A vote has already been registered with this ID");
        }

        List votes = state.get(VOTES, ArrayList.class);
        votes.add(vote);
        hasVoted.add(voterId);
    }

    @SuppressWarnings("unchecked")
    @GET
    @Path("generateVoters")
    @Produces(MediaType.TEXT_HTML)//TODO: REMOVE?
    public String getOrCreateVoters() {
        ArrayList idList = state.get(ID_LIST, ArrayList.class);

        if (idList == null) {
            idList = new ArrayList<String>();
            for (int i = 0; i < 20; i++) {
                idList.add(UUID.randomUUID().toString());
            }
            state.put(ID_LIST, idList);
        }
        return String.join("<br/>", idList);
    }

    @GET
    @Path("result")
    @Produces(MediaType.TEXT_HTML)
    public String getResult() {
        String result = state.get(RESULT, String.class);
        if (result == null) {
            return "<h3> Voting has not yet finished </h3>";
        }
        return "<h3> Voting has finished </h3> <br/>" + result;
    }

    @GET
    @Path("getVotes")
    @Produces(MediaType.APPLICATION_JSON)
    public List getVotes() {
        List list = state.get(VOTES, List.class);
        if (list == null) {
            throw new NotFoundException("Voting has not been initialized");
        }

        return list;
    }
}
