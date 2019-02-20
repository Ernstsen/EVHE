package dk.mmj.evhe.crypto;

import java.math.BigInteger;

public class KeyPair {
    private BigInteger secretKey;
    private PublicKey publicKey;

    KeyPair(BigInteger secretKey, PublicKey publicKey) {
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