package dk.mmj.evhe.crypto;

import dk.mmj.evhe.crypto.keygeneration.KeyGenerationParameters;
import dk.mmj.evhe.crypto.zeroknowledge.DLogProofTestUtils;
import dk.mmj.evhe.crypto.zeroknowledge.DLogProofUtils;
import dk.mmj.evhe.entities.CipherText;
import dk.mmj.evhe.entities.DistKeyGenResult;
import dk.mmj.evhe.entities.PublicKey;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Map;
import java.util.stream.Collectors;

import static dk.mmj.evhe.crypto.TestUtils.getKeyGenParamsFromP2048bitsG2;
import static dk.mmj.evhe.crypto.TestUtils.getKeyGenParamsFromP227G172;
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
        KeyGenerationParameters params = getKeyGenParamsFromP2048bitsG2();
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
                        e -> generateProof(
                                cipherText,
                                e.getValue(),
                                new PublicKey(publicValues.get(e.getKey()), g, q),
                                e.getKey()
                        )));
    }

    @Test
    public void shouldVerifyProof() {
        for (int i = 1; i <= partialDecryptions.size(); i++) {
            BigInteger partialC = partialDecryptions.get(i);
            BigInteger publicValueH = publicValues.get(i);
            CipherText partialDecryption = new CipherText(partialC, cipherText.getD());
            PublicKey partialPublicKey = new PublicKey(publicValueH, g, q);

            boolean verification = DLogProofUtils.verifyProof(cipherText, partialDecryption, partialPublicKey, proofs.get(i), i);

            assertTrue("Couldn't verify proof.", verification);
        }
    }

    @Test
    public void shouldVerifyFakeProof() {
        KeyGenerationParameters params = getKeyGenParamsFromP227G172();

        BigInteger[] polynomial = new BigInteger[]{BigInteger.valueOf(110), BigInteger.valueOf(33)};
        BigInteger q = params.getPrimePair().getQ();
        BigInteger p = params.getPrimePair().getP();
        BigInteger g = params.getGenerator();
        BigInteger y = BigInteger.valueOf(15);
        Map<Integer, BigInteger> secretValues = SecurityUtils.generateSecretValues(polynomial, 3, q);
        Map<Integer, BigInteger> publicValues = SecurityUtils.generatePublicValues(secretValues, g, p);

        BigInteger h = SecurityUtils.combinePartials(publicValues, p);
        PublicKey publicKey = new PublicKey(h, g, q);
        CipherText cipherText = ElGamal.homomorphicEncryption(publicKey, BigInteger.valueOf(1), BigInteger.valueOf(12));

        Map<Integer, BigInteger> partialDec = secretValues.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> SecurityUtils.computePartial(cipherText.getC(), e.getValue(), p)
                ));

        Map<Integer, DLogProofUtils.Proof> fakeproofs = secretValues.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> DLogProofTestUtils.generateFixedProof(
                                cipherText,
                                e.getValue(),
                                new PublicKey(publicValues.get(e.getKey()), g, q), y,
                                e.getKey()
                        )));

        BigInteger partialC = partialDec.get(1);
        BigInteger publicValueH = publicValues.get(1);
        CipherText partialDecryption = new CipherText(partialC, cipherText.getD());
        PublicKey partialPublicKey = new PublicKey(publicValueH, g, q);

        boolean verification = DLogProofUtils.verifyProof(cipherText, partialDecryption, partialPublicKey, fakeproofs.get(1), 1);
        assertTrue("Couldn't verify proof.", verification);
    }
}
