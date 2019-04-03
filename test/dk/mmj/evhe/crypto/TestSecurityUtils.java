package dk.mmj.evhe.crypto;

import dk.mmj.evhe.crypto.entities.KeyPair;
import dk.mmj.evhe.crypto.exceptions.UnableToDecryptException;
import dk.mmj.evhe.server.VoteDTO;
import org.junit.Test;

import static dk.mmj.evhe.crypto.TestUtils.generateKeysFromP2048bitsG2;
import static org.junit.Assert.*;

public class TestSecurityUtils {

    @Test
    public void shouldCreateCorrectVote1() {
        KeyPair keyPair = generateKeysFromP2048bitsG2();
        String id = "TESTID";
        VoteDTO voteDTO = SecurityUtils.generateVote(1, id, keyPair.getPublicKey());

        boolean verified = VoteProofUtils.verifyProof(voteDTO, keyPair.getPublicKey());
        assertTrue("Unable to verify generated vote", verified);

        try {
            int message = ElGamal.homomorphicDecryption(keyPair, voteDTO.getCipherText(), 1000);
            assertEquals("Decrypted message to wrong value", 1, message);
        } catch (UnableToDecryptException e) {
            fail("Unable to decrypt generated ciphertext");
        }
    }

    @Test
    public void shouldCreateCorrectVote0() {
        KeyPair keyPair = generateKeysFromP2048bitsG2();
        String id = "TESTID";
        VoteDTO voteDTO = SecurityUtils.generateVote(0, id, keyPair.getPublicKey());

        boolean verified = VoteProofUtils.verifyProof(voteDTO, keyPair.getPublicKey());
        assertTrue("Unable to verify generated vote", verified);

        try {
            int message = ElGamal.homomorphicDecryption(keyPair, voteDTO.getCipherText(), 1000);
            assertEquals("Decrypted message to wrong value", 0, message);
        } catch (UnableToDecryptException e) {
            fail("Unable to decrypt generated ciphertext");
        }
    }
}