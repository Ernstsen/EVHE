package dk.mmj.evhe.crypto;

import dk.mmj.evhe.crypto.entities.CipherText;
import dk.mmj.evhe.crypto.entities.PublicKey;
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
    static byte[] hash(byte[][] payloads) {
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
}
