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
     * @param primes safe prime p and prime q
     * @param g generator of group Gq
     * @return KeyPair containing secret key and public key
     */
    public static KeyPair generateKeys(Utils.Primes primes, BigInteger g) {
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
        BigInteger p = q.multiply(new BigInteger("2")).add(BigInteger.ONE);
        BigInteger h = g.modPow(secretKey, p);
        return new PublicKey(h, g, q);
    }

    /**
     * Homomorphic encryption
     *
     * @param publicKey the public key
     * @param message the message to encrypt
     * @return the cipher text
     */
    public static CipherText homomorphicEncryption(PublicKey publicKey, BigInteger message) {
        BigInteger r = Utils.getRandomNumModN(publicKey.getQ());
        BigInteger p = publicKey.getQ().multiply(new BigInteger("2")).add(BigInteger.ONE);

        BigInteger c = publicKey.getG().modPow(r, p);
        BigInteger d = publicKey.getG().modPow(message, p).multiply(publicKey.getH().modPow(r, p));
        return new CipherText(c, d);
    }

    /**
     * Homomorphic decryption
     */
    public static int homomorphicDecryption(KeyPair keyPair, CipherText cipherText) {
        BigInteger p = keyPair.getPublicKey().getQ().multiply(new BigInteger("2")).add(BigInteger.ONE);
        BigInteger hr = cipherText.getC().modPow(keyPair.getSecretKey(), p);
        BigInteger message = cipherText.getD().multiply(hr.modInverse(p)).mod(p);
        int b = 0;
        int max = 1000;
        while (b < max) {
            if (message.equals(keyPair.getPublicKey().getG().pow(b))) {
                return b;
            }
            b++;
        }
        return -1;
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

    public static class CipherText {
        private BigInteger c;
        private BigInteger d;

        private CipherText(BigInteger c, BigInteger d) {
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
}
