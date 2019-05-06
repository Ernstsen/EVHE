package dk.mmj.evhe.crypto;

import dk.mmj.evhe.crypto.exceptions.UnableToDecryptException;
import dk.mmj.evhe.crypto.keygeneration.KeyGenerationParameters;
import dk.mmj.evhe.entities.*;

import java.math.BigInteger;
import java.util.Map;

/**
 * Class for handling encryption, decryption and key-generation for the Elgamal encryption scheme
 */
public class ElGamal {

    /**
     * Generates secret and public key from a pair of primes p,q and a generator g
     * <br/>
     * It should be the cases that <code> p=2q+1 </code> and that g is a generator for the cyclic group <code>G_q</code>
     *
     * @param params the pair of primes (p,q) and the generator for G_q
     * @return a KeyPair consisting of a private and secret key
     */
    static KeyPair generateKeys(KeyGenerationParameters params) {
        BigInteger g = params.getGenerator();
        PrimePair primePair = params.getPrimePair();

        BigInteger secretKey = generateSecretKey(primePair.getQ());
        PublicKey publicKey = generatePublicKey(secretKey, g, primePair.getQ());

        return new KeyPair(secretKey, publicKey);
    }

    /**
     * Generates distributed secret and public values
     *
     * @param params           the pair of primes (p,q) and the generator for G_q
     * @param polynomialDegree the degree of the polynomial
     * @param authorities      number of decryption authorities
     * @return a DistKeyGenResult containing all information the TD needs to distribute to decryption authorities
     */
    public static DistKeyGenResult generateDistributedKeys(KeyGenerationParameters params, int polynomialDegree, int authorities) {
        BigInteger g = params.getGenerator();
        PrimePair primePair = params.getPrimePair();

        BigInteger[] polynomial = SecurityUtils.generatePolynomial(polynomialDegree, primePair.getQ());
        Map<Integer, BigInteger> secretValues = SecurityUtils.generateSecretValues(polynomial, authorities, primePair.getQ());
        Map<Integer, BigInteger> publicValues = SecurityUtils.generatePublicValues(secretValues, g, primePair.getP());

        return new DistKeyGenResult(g, primePair.getQ(), secretValues, publicValues);
    }

    /**
     * Generates the secret key
     *
     * @param q prime number used in the cyclic group Gq
     * @return the secret key
     */
    private static BigInteger generateSecretKey(BigInteger q) {
        return SecurityUtils.getRandomNumModN(q);
    }

    /**
     * Generates the public key
     *
     * @param secretKey the secret key
     * @param g         generator for cyclic group Gq
     * @param q         prime number used in the cyclic group Gq
     * @return the public key
     */
    private static PublicKey generatePublicKey(BigInteger secretKey, BigInteger g, BigInteger q) {
        BigInteger p = q.multiply(BigInteger.valueOf(2)).add(BigInteger.ONE);
        BigInteger h = g.modPow(secretKey, p);

        return new PublicKey(h, g, q);
    }

    /**
     * Homomorphic encryption
     *
     * @param publicKey the public key
     * @param message   the message to encrypt
     * @return the cipher text
     */
    static CipherText homomorphicEncryption(PublicKey publicKey, BigInteger message, BigInteger r) {
        BigInteger p = publicKey.getQ().multiply(BigInteger.valueOf(2)).add(BigInteger.ONE);

        BigInteger c = publicKey.getG().modPow(r, p);
        BigInteger d = publicKey.getG().modPow(message, p).multiply(publicKey.getH().modPow(r, p));

        return new CipherText(c, d);
    }

    /**
     * Encrypts the message under the given public key, with the El-Gamal homomorphic encryption scheme
     *
     * @param publicKey public key to be used
     * @param message   message to be encrypted
     * @return encrypted value
     */
    public static CipherText homomorphicEncryption(PublicKey publicKey, BigInteger message) {
        BigInteger r = SecurityUtils.getRandomNumModN(publicKey.getQ());
        return homomorphicEncryption(publicKey, message, r);
    }

    /**
     * Homomorphic decryption
     *
     * @param keyPair    key pair consisting of public and private key
     * @param cipherText cipher text consisting of c and d
     * @return the original number which were encrypted
     */
    static int homomorphicDecryption(KeyPair keyPair, CipherText cipherText, int max) throws UnableToDecryptException {
        BigInteger p = keyPair.getPublicKey().getQ().multiply(BigInteger.valueOf(2)).add(BigInteger.ONE);
        BigInteger hr = cipherText.getC().modPow(keyPair.getSecretKey(), p);
        BigInteger gPowMessage = cipherText.getD().multiply(hr.modInverse(p)).mod(p);

        return findDecryptionValue(gPowMessage, keyPair.getPublicKey().getG(), keyPair.getPublicKey().getP(), max);
    }


    /**
     * Partially decrypts the given c value
     *
     * @param c                value to be partially decrypted
     * @param partialSecretKey partial secret key used to decrypt
     * @param p                p value from public key
     * @return A partial decryption of the C value
     */
    public static BigInteger partialDecryption(BigInteger c, BigInteger partialSecretKey, BigInteger p) {
        return SecurityUtils.computePartial(c, partialSecretKey, p);
    }

    /**
     * Homomorphic addition
     * <br/>
     * Creates a cipher text containing the sum of two original plaintexts, given their ciphertexts
     *
     * @param c1 cipher text of first original plaintext
     * @param c2 cipher text of second original plaintext
     * @return cipher text containing sum of two plaintexts
     */
    public static CipherText homomorphicAddition(CipherText c1, CipherText c2) {
        BigInteger c = c1.getC().multiply(c2.getC());
        BigInteger d = c1.getD().multiply(c2.getD());

        return new CipherText(c, d);
    }

    public static int homomorphicDecryptionFromPartials(CipherText cipherText, BigInteger combinedPartials, BigInteger g, BigInteger p, int max) throws UnableToDecryptException {
        BigInteger gPowMessage = cipherText.getD().multiply(combinedPartials.modInverse(p)).mod(p);

        return findDecryptionValue(gPowMessage, g, p, max);
    }

    private static int findDecryptionValue(BigInteger gPowMessage, BigInteger g, BigInteger p, int max) throws UnableToDecryptException {
        int b = 0;

        while (b < max) {
            if (gPowMessage.equals(g.modPow(BigInteger.valueOf(b), p))) {
                return b;
            }

            b++;
        }

        throw new UnableToDecryptException("Could not decrypt message");
    }
}
