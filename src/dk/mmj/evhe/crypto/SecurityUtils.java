package dk.mmj.evhe.crypto;

import dk.mmj.evhe.crypto.entities.CipherText;
import dk.mmj.evhe.crypto.entities.PublicKey;
import dk.mmj.evhe.crypto.zeroknowledge.VoteProofUtils;
import dk.mmj.evhe.server.VoteDTO;
import org.bouncycastle.crypto.digests.SHA256Digest;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

/**
 * Class used for methods not tied directly to ElGamal
 */
public class SecurityUtils {
    /**
     * Find a random number in the range [1;n)
     *
     * @param n n-1 is upper limit in interval
     * @return random number in range [1;n)
     */
    public static BigInteger getRandomNumModN(BigInteger n) {
        Random random = new SecureRandom();
        BigInteger result = null;

        while (result == null || result.compareTo(new BigInteger("0")) == 0) {
            result = new BigInteger(n.bitLength(), random).mod(n);
        }

        return result;
    }

    /**
     * Hashes an array of values.
     *
     * @param payloads is an array of byte-arrays, containing values to be hashed.
     * @return SHA256 hash of the given payloads.
     */
    public static byte[] hash(byte[][] payloads) {
        SHA256Digest sha256Digest = new SHA256Digest();

        for (byte[] payload : payloads) {
            sha256Digest.update(payload, 0, payload.length);
        }

        byte[] hash = new byte[sha256Digest.getDigestSize()];
        sha256Digest.doFinal(hash, 0);
        return hash;
    }

    /**
     * Generates the ciphertext, vote, and proof.
     *
     * @param vote      the vote as an integer.
     * @param id        the ID of the person voting.
     * @param publicKey the public key used to encrypt the vote.
     * @return a VoteDTO containing the ciphertext, id and proof for the encrypted vote.
     */
    public static VoteDTO generateVote(int vote, String id, PublicKey publicKey) {
        BigInteger r = SecurityUtils.getRandomNumModN(publicKey.getQ());
        CipherText ciphertext = ElGamal.homomorphicEncryption(publicKey, BigInteger.valueOf(vote), r);
        VoteDTO.Proof proof = VoteProofUtils.generateProof(ciphertext, publicKey, r, id, BigInteger.valueOf(vote));

        return new VoteDTO(ciphertext, id, proof);
    }

    /**
     * Generates a polynomial
     *
     * @param degree the degree of the polynomial
     * @param q q-1 specifies the maximum value of coefficients in the polynomial
     * @return a BigInteger array representing the polynomial
     */
    public static BigInteger[] generatePolynomial(int degree, BigInteger q) {
        BigInteger[] polynomial = new BigInteger[degree + 1];
        for (int i = 0; i <= degree; i++) {
            polynomial[i] = getRandomNumModN(q);
        }
        return polynomial;
    }

    /**
     * Generates the secret values for each authority using Shamir's secret sharing scheme
     *
     * @param polynomial the polynomial used for the scheme
     * @param authorities the amount of authorities
     * @return a BigInteger array containing the secret values where index i is the secret for authority i+1
     */
    public static BigInteger[] generateSecretValues(BigInteger[] polynomial, int authorities) {
        BigInteger[] secretValues = new BigInteger[authorities];
        for (int i = 0; i < authorities; i++) {
            int authorityIndex = i + 1;
            BigInteger acc = BigInteger.ZERO;
            for (int j = 0; j < polynomial.length; j++) {
                acc = acc.add(BigInteger.valueOf(authorityIndex).pow(j).multiply(polynomial[j]));
            }
            secretValues[i] = acc;
        }
        return secretValues;
    }

    /**
     * Generates the public values for each authority
     *
     * @param secretValues The secret values
     * @param g generator for group Gq where p = 2q + 1
     * @param p the p value mentioned above
     * @return a BigInteger array containing the public values where index i is the public value for authority i+1
     */
    public static BigInteger[] generatePublicValues(BigInteger[] secretValues, BigInteger g, BigInteger p) {
        BigInteger[] publicValues = new BigInteger[secretValues.length];
        for (int i = 0; i < secretValues.length; i++) {
            publicValues[i] = g.modPow(secretValues[i], p);
        }
        return publicValues;
    }
}
