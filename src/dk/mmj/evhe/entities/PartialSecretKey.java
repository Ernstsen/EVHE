package dk.mmj.evhe.entities;

import java.math.BigInteger;

public class PartialSecretKey {
    private BigInteger secretValue;
    private BigInteger p;

    public PartialSecretKey(BigInteger secretValue, BigInteger p) {
        this.secretValue = secretValue;
        this.p = p;
    }

    public BigInteger getSecretValue() {
        return secretValue;
    }

    public BigInteger getP() {
        return p;
    }
}
