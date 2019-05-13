package dk.mmj.evhe.entities;

import java.math.BigInteger;

/**
 * Entity containing a partial secret key, kept by a {@link dk.mmj.evhe.server.decryptionauthority.DecryptionAuthority}
 */
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
