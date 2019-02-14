package dk.mmj.evhe.crypto;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

/**
 * Class for handling encryption, decryption and key-generation for the Elgamal encryption scheme
 */
public class ElGamal {

    /**
     * Generates both secret key and public key
     *
     * @return KeyPair containing secret key and public key
     */
    public static KeyPair generateKeys() throws KeyPairGenerationException {
        Random randomBits = new SecureRandom();
        BigInteger q = BigInteger.probablePrime(64, randomBits);
        BigInteger g;

        try {
            g = findGeneratorForGq(q);
        } catch (NoGeneratorFoundException e) {
            throw new KeyPairGenerationException("Error occurred doing key generation phase: " + e.getMessage());
        }

        BigInteger secretKey = generateSecretKey(q);

        PublicKey publicKey = generatePublicKey(secretKey, g, q);

        return new KeyPair(secretKey, publicKey);
    }

    /**
     * Finds a suitable generator g for the cyclic group Gq
     *
     * @param q prime number used in the cyclic group Gq
     * @return generator g for cyclic group Gq
     * @throws NoGeneratorFoundException
     */
    private static BigInteger findGeneratorForGq(BigInteger q) throws NoGeneratorFoundException {
        BigInteger g = new BigInteger("2");

        // While the prime number q is greater than the potential generator g
        while (q.compareTo(g) == 1) {
            if (q.gcd(g) == BigInteger.ONE) {
                return g;
            }
            g.add(BigInteger.ONE);
        }

        throw new NoGeneratorFoundException("No generator g for cyclic group Gq was found");
    }

    /**
     * Generates the secret key
     *
     * @param q prime number used in the cyclic group Gq
     * @return the secret key
     */
    private static BigInteger generateSecretKey(BigInteger q) {
        Random randomBits = new SecureRandom();
        BigInteger secretKey = new BigInteger(q.bitLength(), randomBits);

        // While q is greater or equal to the secret key (the secret key most lie in the interval [0,q-1])
        while (q.compareTo(secretKey) >= 0) {
            secretKey = new BigInteger(q.bitLength(), randomBits);
        }

        return secretKey;
    }

    /**
     * Generates the public key
     *
     * @param secretKey the secret key
     * @param g generator for cyclic group Gq
     * @param q prime number used in the cyclic group Gq
     * @return the public key
     */
    private static PublicKey generatePublicKey(BigInteger secretKey, BigInteger g, BigInteger q) {
        BigInteger h = g.modPow(secretKey, q);
        return new PublicKey(h, g, q);
    }

    public static class KeyPair {
        private BigInteger secretKey;
        private PublicKey publicKey;

        private KeyPair(BigInteger secretKey, PublicKey publicKey) {
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
}
