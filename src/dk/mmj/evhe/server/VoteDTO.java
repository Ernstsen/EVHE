package dk.mmj.evhe.server;

import dk.mmj.evhe.crypto.CipherText;

import java.math.BigInteger;

/**
 * Simple DTO object for casting votes
 */
@SuppressWarnings("JavaDocs, unused")
public class VoteDTO {
    private CipherText cipherText;
    private String id;
    private Proof proof;

    public VoteDTO() {
    }

    public VoteDTO(CipherText cipherText, String id, Proof proof) {
        this.cipherText = cipherText;
        this.id = id;
        this.proof = proof;
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

    public Proof getProof() {
        return proof;
    }

    public void setProof(Proof proof) {
        this.proof = proof;
    }

    /**
     * DTO class for proof that votes is either 0 or 1
     */
    static class Proof {
        private BigInteger a;
        private BigInteger z;

        public Proof() {
        }

        public Proof(BigInteger a, BigInteger z) {
            this.a = a;
            this.z = z;
        }

        public BigInteger getA() {
            return a;
        }

        public void setA(BigInteger a) {
            this.a = a;
        }

        public BigInteger getZ() {
            return z;
        }

        public void setZ(BigInteger z) {
            this.z = z;
        }
    }
}
