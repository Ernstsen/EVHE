package dk.mmj.evhe.crypto.zeroknowledge;

import dk.mmj.evhe.entities.CipherText;
import dk.mmj.evhe.entities.PublicKey;

import java.math.BigInteger;

public class DLogProofTestUtils {
    public static DLogProofUtils.Proof generateFixedProof(CipherText cipherText, BigInteger secretValue, PublicKey publicKey, BigInteger y, int id) {
        return DLogProofUtils.generateProof(cipherText, secretValue, publicKey, y, id);
    }
}
