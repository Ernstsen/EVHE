package dk.mmj.evhe.server.bulletinboard;


import dk.mmj.evhe.entities.*;
import dk.mmj.evhe.server.ServerState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.math.BigInteger;
import java.util.*;

import static dk.mmj.evhe.server.bulletinboard.BulletinBoard.*;

@Path("/")
public class BulletinBoardResource {
    private static final String PUBLIC_INFO = "publicInfo";
    private static Logger logger = LogManager.getLogger(BulletinBoardResource.class);
    private ServerState state = ServerState.getInstance();

    @GET
    @Path("type")
    @Produces(MediaType.TEXT_HTML)
    public String getType() {
        logger.info("Received request for server type");
        return "<b>ServerType:</b> Bulletin Board";
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
        if (publicKey == null) {
            logger.warn("A submitted key CANNOT be null");
            throw new NotAllowedException("Key was null");

        }

        state.put(PUBLIC_KEY, publicKey);
    }

    @POST
    @Path("postPublicInfo")
    @Consumes(MediaType.APPLICATION_JSON)
    public void initialize(PublicInformationEntity info) {
        addToList(PUBLIC_INFO, info);
    }

    @GET
    @Path("getPublicInfo")
    @Produces(MediaType.APPLICATION_JSON)
    @SuppressWarnings("unchecked")
    public PublicInfoList getPublicInfos() {
        List<PublicInformationEntity> list = state.get(PUBLIC_INFO, List.class);

        if (list == null) {
            logger.warn("Attempt to fetch public infos before they were created");
            throw new NotFoundException();
        }

        return new PublicInfoList(list);
    }

    @POST
    @Path("vote")
    @Consumes(MediaType.APPLICATION_JSON)
    @SuppressWarnings("unchecked")
    public void Vote(VoteDTO vote) {
        Set hasVoted = state.get(HAS_VOTED, HashSet.class);
        String voterId = vote.getId();


        if (hasVoted.contains(voterId)) {
            logger.warn("Voter with id=" + voterId + " attempted to vote more than once");
            throw new NotAllowedException("A vote has already been registered with this ID");
        }

        List votes = state.get(VOTES, ArrayList.class);
        votes.add(new PersistedVote(vote));
        hasVoted.add(voterId);
    }

    /**
     * @return List of {@link BigInteger} which is partial decryptions
     */
    @GET
    @Path("result")
    @Produces(MediaType.APPLICATION_JSON)
    @SuppressWarnings("unchecked")
    public ResultList getResult() {
        return new ResultList(state.get(RESULT, List.class));
    }

    @POST
    @Path("result")
    @Consumes(MediaType.APPLICATION_JSON)
    public void postResult(PartialResult partialDecryption) {
        addToList(RESULT, partialDecryption);
    }

    @SuppressWarnings("unchecked")
    private void addToList(String key, Object element) {
        List list = state.get(key, List.class);

        if (list == null) {
            list = new ArrayList();
            state.put(key, list);
        }

        list.add(element);
    }

    @SuppressWarnings("unchecked")
    @GET
    @Path("getVotes")
    @Produces(MediaType.APPLICATION_JSON)
    public VoteList getVotes() {
        List<PersistedVote> list = state.get(VOTES, List.class);

        if (list == null) {
            throw new NotFoundException("Voting has not been initialized");
        }

        return new VoteList(list);
    }

    @GET
    @Path("getCurrentTime")
    @Produces(MediaType.APPLICATION_JSON)
    public String getCurrentTime() {
        return Long.toString(new Date().getTime());
    }
}
