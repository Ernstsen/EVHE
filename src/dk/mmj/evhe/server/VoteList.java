package dk.mmj.evhe.server;

import java.util.List;

@SuppressWarnings("JavaDocs, unused")
public class VoteList {

    private List<VoteDTO> votes;

    public VoteList(List<VoteDTO> votes) {
        this.votes = votes;
    }

    public VoteList() {
    }

    public List<VoteDTO> getVotes() {
        return votes;
    }

    public void setVotes(List<VoteDTO> votes) {
        this.votes = votes;
    }
}
