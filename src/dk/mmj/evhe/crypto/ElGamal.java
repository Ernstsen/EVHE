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
    public static KeyPair generateKeys(int primeBitLength, int primeCertainty) {
        Utils.Primes primes = Utils.findPrimes(primeBitLength, primeCertainty);
        BigInteger g = Utils.findGeneratorForGq(primes);

        BigInteger secretKey = generateSecretKey(primes.getQ());
        PublicKey publicKey = generatePublicKey(secretKey, g, primes.getQ());

        return new KeyPair(secretKey, publicKey);
    }

    /**
     * Generates the secret key
     *
     * @param q prime number used in the cyclic group Gq
     * @return the secret key
     */
    private static BigInteger generateSecretKey(BigInteger q) {
        return Utils.getRandomNumModN(q);
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

    public static class PublicKey {
        private BigInteger h, g, q;

        /**
         * Unused object mapper constructor
         */
        @SuppressWarnings("unused")
        private PublicKey() {}

        private PublicKey (BigInteger h, BigInteger g, BigInteger q) {
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
}
