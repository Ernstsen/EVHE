package dk.mmj.evhe.crypto;

import dk.mmj.evhe.crypto.keygeneration.KeyGenerationParameters;
import dk.mmj.evhe.crypto.zeroknowledge.DLogProofUtils;
import dk.mmj.evhe.entities.CipherText;
import dk.mmj.evhe.entities.DistKeyGenResult;
import dk.mmj.evhe.entities.PublicKey;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Map;
import java.util.stream.Collectors;

import static dk.mmj.evhe.crypto.TestUtils.getKeyGenParamsFromP11G2;
import static dk.mmj.evhe.crypto.TestUtils.getKeyGenParamsFromP2048bitsG2;
import static dk.mmj.evhe.crypto.zeroknowledge.DLogProofUtils.generateProof;
import static org.junit.Assert.assertTrue;

public class TestDLogProofUtils {
    private BigInteger g;
    private BigInteger q;
    private Map<Integer, BigInteger> publicValues;
    private Map<Integer, BigInteger> partialDecryptions;
    private CipherText cipherText;
    private Map<Integer, DLogProofUtils.Proof> proofs;

    @Before
    public void setupProofsForPartialDecryptions() {
        KeyGenerationParameters params = getKeyGenParamsFromP11G2();
        DistKeyGenResult distKeyGenResult = ElGamal.generateDistributedKeys(params, 1, 3);

        g = distKeyGenResult.getG();
        q = distKeyGenResult.getQ();
        publicValues = distKeyGenResult.getPublicValues();

        BigInteger h = SecurityUtils.combinePartials(publicValues, distKeyGenResult.getP());
        PublicKey publicKey = new PublicKey(h, g, q);
        cipherText = ElGamal.homomorphicEncryption(publicKey, BigInteger.valueOf(1));

        partialDecryptions = distKeyGenResult.getSecretValues().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> SecurityUtils.computePartial(cipherText.getC(), e.getValue(), distKeyGenResult.getP())
                ));

        proofs = distKeyGenResult.getSecretValues().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> generateProof(cipherText, e.getValue(),
                                new PublicKey(publicValues.get(e.getKey()), g, q)
                        )));
    }

    @Test
    public void shouldVerifyProof() {
        BigInteger partialC = partialDecryptions.get(1);
        BigInteger publicValueH = publicValues.get(1);
        CipherText partialDecryption = new CipherText(partialC, cipherText.getD());
        PublicKey partialPublicKey = new PublicKey(publicValueH, g, q);

        boolean verification = DLogProofUtils.verifyProof(cipherText, partialDecryption, partialPublicKey, proofs.get(1));

        assertTrue("Couldn't verify proof.", verification);
    }
}
