package dk.mmj.evhe.crypto;

import java.math.BigInteger;

public class PublicKey {
    private BigInteger h, g, q;

    public PublicKey (BigInteger h, BigInteger g, BigInteger q) {
        this.g = g;
        this.q = q;
        this.h = h;
    }

    public BigInteger getH() {
        return h;
    }

    public BigInteger getG() {
        return g;
    }

    public BigInteger getQ() {
        return q;
    }
}
