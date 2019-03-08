package dk.mmj.evhe.server;

import dk.mmj.evhe.crypto.CipherText;

@SuppressWarnings("JavaDocs, unused")
public class VoteDTO {
    private CipherText cipherText;
    private String id;

    public VoteDTO(CipherText cipherText, String id) {
        this.cipherText = cipherText;
        this.id = id;
    }

    public CipherText getCipherText() {
        return cipherText;
    }

    public void setCipherText(CipherText cipherText) {
        this.cipherText = cipherText;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
