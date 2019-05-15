package dk.mmj.evhe.crypto.zeroknowledge;

import dk.mmj.evhe.crypto.SecurityUtils;
import dk.mmj.evhe.entities.CipherText;
import dk.mmj.evhe.entities.PublicKey;
import dk.mmj.evhe.entities.VoteDTO;

import java.math.BigInteger;

public class VoteProofUtils {

    /**
     * Method for generating zero-knowledge proof for vote that is either 0 or 1
     *
     * @param cipherText ciphertext from encrypted vote
     * @param publicKey  public key the vote is encrypted under
     * @param witness    the r value from encryption
     * @param id         voter id
     * @param vote       what was voted - either 0 or 1
     * @return the zero-knowledge proof
     */
    @SuppressWarnings("DuplicateExpressions")
    public static VoteDTO.Proof generateProof(CipherText cipherText, PublicKey publicKey, BigInteger witness, String id, BigInteger vote) {
        int v = (vote.intValue() > 0) ? 1 : 0; // For unit-test purposes.

        BigInteger[] e = new BigInteger[2];
        BigInteger[] z = new BigInteger[2];
        BigInteger[] a = new BigInteger[2];
        BigInteger[] b = new BigInteger[2];
        BigInteger g = publicKey.getG();
        BigInteger h = publicKey.getH();
        BigInteger c = cipherText.getC();
        BigInteger d = cipherText.getD();
        BigInteger q = publicKey.getQ();
        BigInteger p = publicKey.getP();

        int fakeIndex = (1 - v);

        BigInteger y = SecurityUtils.getRandomNumModN(q);
        e[fakeIndex] = SecurityUtils.getRandomNumModN(q);
        z[fakeIndex] = SecurityUtils.getRandomNumModN(q);

        a[fakeIndex] = g.modPow(z[fakeIndex], p).multiply(c.modPow(e[fakeIndex], p)).mod(p);

        if (v == 1) {
            b[fakeIndex] = h.modPow(z[fakeIndex], p).multiply(d.modPow(e[fakeIndex], p)).mod(p);
        } else {
            b[fakeIndex] = h.modPow(z[fakeIndex], p).multiply(d.multiply(g.modInverse(p)).modPow(e[fakeIndex], p)).mod(p);
        }

        a[v] = g.modPow(y, p);
        b[v] = h.modPow(y, p);

        BigInteger s = new BigInteger(
                SecurityUtils.hash(new byte[][]{
                        a[0].toByteArray(),
                        b[0].toByteArray(),
                        a[1].toByteArray(),
                        b[1].toByteArray(),
                        c.toByteArray(),
                        d.toByteArray(),
                        id.getBytes()
                })).mod(q);

        e[v] = s.subtract(e[fakeIndex]).mod(q);
        z[v] = y.subtract(e[v].multiply(witness)).mod(q);

        return new VoteDTO.Proof(e[0], e[1], z[0], z[1]);
    }


    /**
     * Method for verifying that the zero-knowledge proof of a vote is correct
     *
     * @param vote      vote to be verified
     * @param publicKey key the vote is encrypted under
     * @return whether the vote could be verified
     */
    public static boolean verifyProof(VoteDTO vote, PublicKey publicKey) {
        BigInteger e0 = vote.getProof().getE0();
        BigInteger e1 = vote.getProof().getE1();
        BigInteger z0 = vote.getProof().getZ0();
        BigInteger z1 = vote.getProof().getZ1();
        BigInteger g = publicKey.getG();
        BigInteger h = publicKey.getH();
        BigInteger p = publicKey.getP();
        BigInteger c = vote.getCipherText().getC();
        BigInteger d = vote.getCipherText().getD();

        BigInteger a0 = g.modPow(z0, p).multiply(c.modPow(e0, p)).mod(p);
        BigInteger b0 = h.modPow(z0, p).multiply(d.modPow(e0, p)).mod(p);
        BigInteger a1 = g.modPow(z1, p).multiply(c.modPow(e1, p)).mod(p);
        BigInteger b1 = h.modPow(z1, p).multiply(d.multiply(g.modInverse(p)).modPow(e1, p)).mod(p);

        BigInteger s = new BigInteger(
                SecurityUtils.hash(new byte[][]{
                        a0.toByteArray(),
                        b0.toByteArray(),
                        a1.toByteArray(),
                        b1.toByteArray(),
                        c.toByteArray(),
                        d.toByteArray(),
                        vote.getId().getBytes()
                })).mod(publicKey.getQ());

        BigInteger e = e0.add(e1);

        return e.mod(publicKey.getQ()).equals(s);
    }
}

