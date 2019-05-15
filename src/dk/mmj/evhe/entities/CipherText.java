package dk.mmj.evhe.entities;

import java.math.BigInteger;

@SuppressWarnings("JavaDocs, unused")
public class CipherText {
    private BigInteger c;
    private BigInteger d;

    /**
     * Empty {@link com.fasterxml.jackson.databind.ObjectMapper} constructor
     */
    public CipherText() {
    }

    public CipherText(BigInteger c, BigInteger d) {
        this.c = c;
        this.d = d;
    }

    public BigInteger getC() {
        return c;
    }

    public void setC(BigInteger c) {
        this.c = c;
    }

    public BigInteger getD() {
        return d;
    }

    public void setD(BigInteger d) {
        this.d = d;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof CipherText) {
            return d.equals(((CipherText) o).d) && c.equals(((CipherText) o).c);
        }

        return super.equals(o);
    }
}