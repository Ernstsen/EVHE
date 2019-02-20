package dk.mmj.evhe.crypto;

import java.math.BigInteger;

public class CipherText {
    private BigInteger c;
    private BigInteger d;

    CipherText(BigInteger c, BigInteger d) {
        this.c = c;
        this.d = d;
    }

    public BigInteger getC() {
        return c;
    }

    public BigInteger getD() {
        return d;
    }
}