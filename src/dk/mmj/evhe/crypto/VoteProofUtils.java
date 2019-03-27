package dk.mmj.evhe.crypto;

import dk.mmj.evhe.server.VoteDTO;

import java.math.BigInteger;

public class VoteProofUtils {
    @SuppressWarnings("DuplicateExpressions")
    static VoteDTO.Proof generateProof(CipherText cipherText, PublicKey publicKey, BigInteger witness, String id, int vote) {
        BigInteger[] e = new BigInteger[2];
        BigInteger[] z = new BigInteger[2];
        BigInteger[] a = new BigInteger[2];
        BigInteger[] b = new BigInteger[2];
        BigInteger g = publicKey.getG();
        BigInteger h = publicKey.getH();
        BigInteger c = cipherText.getC();
        BigInteger d = cipherText.getD();
        BigInteger q = publicKey.getQ();
        BigInteger p = q.multiply(BigInteger.valueOf(2)).add(BigInteger.valueOf(2));
        BigInteger y = Utils.getRandomNumModN(q);
        int fakeIndex = (1 - vote);

        e[fakeIndex] = Utils.getRandomNumModN(q);
        z[fakeIndex] = Utils.getRandomNumModN(q);
        a[fakeIndex] = g.modPow(z[fakeIndex], p).divide(c.modPow(e[fakeIndex], p));

        if (vote == 1) {
            b[fakeIndex] = h.modPow(z[fakeIndex], p).divide(d.modPow(e[fakeIndex], p));
        } else {
            b[fakeIndex] = h.modPow(z[fakeIndex], p).divide(d.divide(g).modPow(e[fakeIndex], p));
        }

        a[vote] = g.modPow(y, p);
        b[vote] = h.modPow(y, p);

        BigInteger s = new BigInteger(
                Utils.hash(new byte[][]{
                        a[0].toByteArray(),
                        b[0].toByteArray(),
                        a[1].toByteArray(),
                        b[1].toByteArray(),
                        c.toByteArray(),
                        d.toByteArray(),
                        id.getBytes()
                }));

        e[vote] = s.subtract(e[fakeIndex]);
        z[vote] = y.add(e[vote].multiply(witness)).mod(q);

        return new VoteDTO.Proof(e[0], e[1], z[0], z[1]);
    }
}
