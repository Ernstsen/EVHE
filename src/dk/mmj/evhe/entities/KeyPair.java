package dk.mmj.evhe.entities;

import java.math.BigInteger;

/**
 * "dumb" object for keeping a keypair
 */
public class KeyPair {
    private BigInteger secretKey;
    private PublicKey publicKey;

    public KeyPair(BigInteger secretKey, PublicKey publicKey) {
        this.secretKey = secretKey;
        this.publicKey = publicKey;
    }

    public BigInteger getSecretKey() {
        return secretKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }
}