package dk.mmj.evhe.crypto;

import dk.mmj.evhe.entities.CipherText;
import dk.mmj.evhe.entities.KeyPair;
import dk.mmj.evhe.entities.PublicKey;
import dk.mmj.evhe.crypto.zeroknowledge.VoteProofUtils;
import dk.mmj.evhe.entities.VoteDTO;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

import static dk.mmj.evhe.crypto.TestUtils.generateKeysFromP2048bitsG2;
import static org.junit.Assert.assertFalse;

public class TestVoteProofUtils {
    private static String id = "testid42";

    private boolean createCiphertextAndProof(int vote, String cipherTextId, String proofId) {
        KeyPair keyPair = generateKeysFromP2048bitsG2();

        PublicKey publicKey = keyPair.getPublicKey();

        BigInteger r = SecurityUtils.getRandomNumModN(publicKey.getQ());
        BigInteger v = BigInteger.valueOf(vote);

        CipherText cipherText = ElGamal.homomorphicEncryption(publicKey, v, r);

        VoteDTO.Proof proof = VoteProofUtils.generateProof(cipherText, publicKey, r, cipherTextId, v);

        VoteDTO voteDTO = new VoteDTO(cipherText, proofId, proof);

        return VoteProofUtils.verifyProof(voteDTO, publicKey);
    }

    @Test
    public void shouldVerifyProofWhenVoteIs1() {
        Assert.assertTrue("Proof verification failed.", createCiphertextAndProof(1, id, id));
    }

    @Test
    public void shouldVerifyProofWhenVoteIs0() {
        Assert.assertTrue("Proof verification failed.", createCiphertextAndProof(0, id, id));
    }

    @Test
    public void shouldNotVerifyProofWhenVoteIs1AndIdIsWrong() {
        assertFalse("Proof verification failed.", createCiphertextAndProof(1, id, "testid43"));
    }

    @Test
    public void shouldNotVerifyProofWhenVoteIs0AndIdIsWrong() {
        assertFalse("Proof verification failed.", createCiphertextAndProof(0, id, "randomstring"));
    }

    @Test
    public void shouldNotVerifyProofWhenVoteIs3() {
        assertFalse("Proof verification succeeded, but should fail.", createCiphertextAndProof(3, id, id));
    }

    @Test
    public void shouldFailFromDifferentRValues() {
        KeyPair keyPair = generateKeysFromP2048bitsG2();
        BigInteger r = SecurityUtils.getRandomNumModN(keyPair.getPublicKey().getQ());

        BigInteger r2;

        do {
            r2 = SecurityUtils.getRandomNumModN(keyPair.getPublicKey().getQ());
        } while (r2.equals(r));//Make sure r2 is not equals to r


        BigInteger v = BigInteger.valueOf(1);
        CipherText cipherText = ElGamal.homomorphicEncryption(keyPair.getPublicKey(), v, r);

        VoteDTO.Proof proof = VoteProofUtils.generateProof(cipherText, keyPair.getPublicKey(), r2, "ID", v);

        VoteDTO voteDTO = new VoteDTO(cipherText, "ID", proof);

        boolean verify = VoteProofUtils.verifyProof(voteDTO, keyPair.getPublicKey());

        assertFalse("Verified proof where r was different from ciphertext", verify);
    }

    @Test
    public void shouldFailIncorrectCiphertext() {
        KeyPair keyPair = generateKeysFromP2048bitsG2();
        BigInteger r = SecurityUtils.getRandomNumModN(keyPair.getPublicKey().getQ());

        BigInteger r2;

        do {
            r2 = SecurityUtils.getRandomNumModN(keyPair.getPublicKey().getQ());
        } while (r2.equals(r));//Make sure r2 is not equals to r


        BigInteger v = BigInteger.valueOf(1);
        CipherText cipherText = ElGamal.homomorphicEncryption(keyPair.getPublicKey(), v, r);
        CipherText cipherText2 = ElGamal.homomorphicEncryption(keyPair.getPublicKey(), BigInteger.valueOf(0), r);

        VoteDTO.Proof proof = VoteProofUtils.generateProof(cipherText, keyPair.getPublicKey(), r2, "ID", v);

        VoteDTO voteDTO = new VoteDTO(cipherText2, "ID", proof);

        boolean verify = VoteProofUtils.verifyProof(voteDTO, keyPair.getPublicKey());

        assertFalse("Verified proof where ciphertext had been replaced", verify);
    }
}

