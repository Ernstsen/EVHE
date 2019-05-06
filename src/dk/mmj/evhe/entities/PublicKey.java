package dk.mmj.evhe.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigInteger;

@SuppressWarnings("unused")
public class PublicKey {
    private BigInteger h, g, q;

    /**
     * Unused object mapper constructor
     */
    @SuppressWarnings("unused")
    private PublicKey() {
    }

    public PublicKey(BigInteger h, BigInteger g, BigInteger q) {
        this.g = g;
        this.q = q;
        this.h = h;
    }

    public BigInteger getH() {
        return h;
    }

    public void setH(BigInteger h) {
        this.h = h;
    }

    public BigInteger getG() {
        return g;
    }

    public void setG(BigInteger g) {
        this.g = g;
    }

    public BigInteger getQ() {
        return q;
    }

    public void setQ(BigInteger q) {
        this.q = q;
    }

    @JsonIgnore
    public BigInteger getP() {
        return q.multiply(BigInteger.valueOf(2)).add(BigInteger.ONE);
    }
}