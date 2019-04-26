package dk.mmj.evhe.crypto.zeroknowledge;

import dk.mmj.evhe.crypto.SecurityUtils;
import dk.mmj.evhe.crypto.entities.CipherText;
import dk.mmj.evhe.crypto.entities.PublicKey;

import java.math.BigInteger;

import static dk.mmj.evhe.crypto.SecurityUtils.getRandomNumModN;

public class DLogProofUtils {

    /**
     * Generates a proof of discrete logarithms equality for a partial decryption
     *
     * @param cipherText the cipher text computed using homomorphic addition
     * @param secretValue the secret value s_i
     * @param publicKey the public key containing g, q, p and h_i which is specific for authority i
     * @return the proof containing the challenge e and answer z
     */
    public static Proof generateProof(CipherText cipherText, BigInteger secretValue, PublicKey publicKey) {
        BigInteger c = cipherText.getC();
        BigInteger p = publicKey.getP();
        BigInteger q = publicKey.getQ();

        BigInteger y = getRandomNumModN(q);
        BigInteger a = c.modPow(y, p);
        BigInteger b = publicKey.getG().modPow(y, p);
        BigInteger e = new BigInteger(
                SecurityUtils.hash(new byte[][]{
                        a.toByteArray(),
                        b.toByteArray(),
                        c.modPow(secretValue, p).toByteArray(),
                        publicKey.getH().toByteArray()
                })).mod(q);
        BigInteger z = y.add(secretValue.multiply(e)).mod(q);
        return new Proof(e, z);
    }

    /**
     * Verifies whether the given proof of discrete logarithms equality for a partial decryption is correct
     *
     * @param cipherText the cipher text computed using homomorphic addition
     * @param partialDecryption partial decryption of cipherText using the secret value s_i
     * @param publicKey the public key containing g, q, p and h_i which is specific for authority i
     * @param proof the proof of discrete logarithm equality for computePartial
     * @return whether the partial decryption could be verified
     */
    public static boolean verifyProof(CipherText cipherText, CipherText partialDecryption, PublicKey publicKey, Proof proof) {
        BigInteger p = publicKey.getP();
        BigInteger a = cipherText.getC().modPow(proof.getZ(), p)
                                        .multiply(partialDecryption.getC().modPow(proof.getE(), p).modInverse(p));
        BigInteger b = publicKey.getG().modPow(proof.getZ(), p)
                .multiply(publicKey.getH().modPow(proof.getE(), p).modInverse(p));

        BigInteger s = new BigInteger(
                SecurityUtils.hash(new byte[][]{
                        a.toByteArray(),
                        b.toByteArray(),
                        partialDecryption.getC().toByteArray(),
                        publicKey.getH().toByteArray()
                })).mod(publicKey.getQ());

        return proof.getE().equals(s);
    }

    public static class Proof {
        private BigInteger e;
        private BigInteger z;

        public Proof(BigInteger e, BigInteger z) {
            this.e = e;
            this.z = z;
        }

        public BigInteger getE() {
            return e;
        }

        public void setE(BigInteger e) {
            this.e = e;
        }

        public BigInteger getZ() {
            return z;
        }

        public void setZ(BigInteger z) {
            this.z = z;
        }
    }
}
