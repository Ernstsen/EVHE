package dk.mmj.evhe.entities;

import dk.mmj.evhe.crypto.zeroknowledge.DLogProofUtils;

import java.math.BigInteger;

@SuppressWarnings("unused, JavaDocs")
public class PartialResult {
    private Integer id;
    private BigInteger d;
    private BigInteger result;
    private DLogProofUtils.Proof proof;

    public PartialResult() {
    }

    public PartialResult(Integer id, BigInteger d, BigInteger result, DLogProofUtils.Proof proof) {
        this.id = id;
        this.d = d;
        this.result = result;
        this.proof = proof;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public BigInteger getD() {
        return d;
    }

    public void setD(BigInteger d) {
        this.d = d;
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
}

