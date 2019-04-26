package dk.mmj.evhe.entities;

import java.util.Date;

/**
 * {@link VoteDTO} with timestamp
 */
@SuppressWarnings("unused, JavaDocs")
public class PersistedVote extends VoteDTO {

    private Date ts;

    public PersistedVote() {
    }

    public PersistedVote(CipherText cipherText, String id, Proof proof, Date ts) {
        super(cipherText, id, proof);
        this.ts = ts;
    }

    public PersistedVote(VoteDTO voteDTO) {
        super(voteDTO.getCipherText(), voteDTO.getId(), voteDTO.getProof());
        this.ts = new Date();
    }

    public Date getTs() {
        return ts;
    }

    public void setTs(Date ts) {
        this.ts = ts;
    }
}
