package dk.mmj.evhe.entities;

import dk.mmj.evhe.crypto.zeroknowledge.DLogProofUtils;

import java.math.BigInteger;

@SuppressWarnings("unused, JavaDocs")
public class PartialResult {
    private Integer id;
    private BigInteger result;
    private DLogProofUtils.Proof proof;
    private CipherText cipherText;
    private int votes;

    public PartialResult() {
    }

    public PartialResult(Integer id, BigInteger result, DLogProofUtils.Proof proof, CipherText cipherText, int votes) {
        this.id = id;
        this.result = result;
        this.proof = proof;
        this.cipherText = cipherText;
        this.votes = votes;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public BigInteger getResult() {
        return result;
    }

    public void setResult(BigInteger result) {
        this.result = result;
    }

    public DLogProofUtils.Proof getProof() {
        return proof;
    }

    public void setProof(DLogProofUtils.Proof proof) {
        this.proof = proof;
    }

    public CipherText getCipherText() {
        return cipherText;
    }

    public void setCipherText(CipherText cipherText) {
        this.cipherText = cipherText;
    }

    public int getVotes() {
        return votes;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }
}

