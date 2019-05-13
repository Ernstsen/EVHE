package dk.mmj.evhe.entities;

import dk.mmj.evhe.crypto.zeroknowledge.DLogProofUtils;

import java.math.BigInteger;

@SuppressWarnings("unused, JavaDocs")
public class PartialResult {
    private Integer id;
    private BigInteger result;
    private DLogProofUtils.Proof proof;
    private CipherText c;
    private int votes;

    public PartialResult() {
    }

    public PartialResult(Integer id, BigInteger result, DLogProofUtils.Proof proof, CipherText c, int votes) {
        this.id = id;
        this.result = result;
        this.proof = proof;
        this.c = c;
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

    public CipherText getC() {
        return c;
    }

    public void setC(CipherText c) {
        this.c = c;
    }

    public int getVotes() {
        return votes;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }
}

