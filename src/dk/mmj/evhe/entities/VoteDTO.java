package dk.mmj.evhe.entities;

import java.math.BigInteger;

/**
 * Simple DTO object for casting votes
 */
@SuppressWarnings("JavaDocs, unused")
public class VoteDTO {
    private CipherText cipherText;
    private String id;
    private Proof proof;

    VoteDTO() {
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
    public static class Proof {
        private BigInteger e0;
        private BigInteger e1;
        private BigInteger z0;
        private BigInteger z1;

        public Proof() {
        }

        public Proof(BigInteger e0, BigInteger e1, BigInteger z0, BigInteger z1) {
            this.e0 = e0;
            this.e1 = e1;
            this.z0 = z0;
            this.z1 = z1;
        }

        public BigInteger getE0() {
            return e0;
        }

        public void setE0(BigInteger e0) {
            this.e0 = e0;
        }

        public BigInteger getE1() {
            return e1;
        }

        public void setE1(BigInteger e1) {
            this.e1 = e1;
        }

        public BigInteger getZ0() {
            return z0;
        }

        public void setZ0(BigInteger z0) {
            this.z0 = z0;
        }

        public BigInteger getZ1() {
            return z1;
        }

        public void setZ1(BigInteger z1) {
            this.z1 = z1;
        }
    }
}
