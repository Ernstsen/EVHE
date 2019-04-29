package dk.mmj.evhe.crypto;

import dk.mmj.evhe.entities.*;
import dk.mmj.evhe.crypto.exceptions.UnableToDecryptException;
import dk.mmj.evhe.crypto.keygeneration.KeyGenerationParameters;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Map;
import java.util.stream.Collectors;

import static dk.mmj.evhe.crypto.TestUtils.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestElGamal {
    private int maxIterations = 1000;

    @Test
    public void shouldBeAbleToEncryptAndDecrypt0() {
        try {
            KeyPair keyPair = generateKeysFromP11G2();
            BigInteger r = SecurityUtils.getRandomNumModN(keyPair.getPublicKey().getQ());
            CipherText cipherText = ElGamal.homomorphicEncryption(keyPair.getPublicKey(), new BigInteger("0"), r);
            int b = ElGamal.homomorphicDecryption(keyPair, cipherText, maxIterations);

            Assert.assertEquals(0, b);
        } catch (UnableToDecryptException e) {
            fail("Was unable to decrypt encrypted value, with message: " + e.getMessage());
        }
    }

    @Test
    public void shouldBeAbleToEncryptAndDecrypt1() {
        try {
            KeyPair keyPair = generateKeysFromP11G2();
            BigInteger r = SecurityUtils.getRandomNumModN(keyPair.getPublicKey().getQ());
            CipherText cipherText = ElGamal.homomorphicEncryption(keyPair.getPublicKey(), BigInteger.ONE, r);
            int b = ElGamal.homomorphicDecryption(keyPair, cipherText, maxIterations);

            Assert.assertEquals(1, b);
        } catch (UnableToDecryptException e) {
            fail("Was unable to decrypt encrypted value, with message: " + e.getMessage());
        }
    }

    @Test
    public void shouldBeAbleToEncryptAndDecryptRandom() {
        try {
            KeyPair keyPair = generateKeysFromP2048bitsG2();
            BigInteger message = SecurityUtils.getRandomNumModN(BigInteger.valueOf(maxIterations));
            CipherText cipherText = ElGamal.homomorphicEncryption(keyPair.getPublicKey(), message);
            int m = ElGamal.homomorphicDecryption(keyPair, cipherText, maxIterations);

            assertEquals(message.intValue(), m);
        } catch (UnableToDecryptException e) {
            fail("Was unable to decrypt encrypted value, with message: " + e.getMessage());
        }

    }

    @Test
    public void testHomomorphicAdditionWhenPlaintextsBothAre1() {
        try {
            KeyPair keyPair = generateKeysFromP11G2();
            BigInteger r = SecurityUtils.getRandomNumModN(keyPair.getPublicKey().getQ());
            CipherText cipherText1 = ElGamal.homomorphicEncryption(keyPair.getPublicKey(), BigInteger.ONE, r);
            CipherText cipherText2 = ElGamal.homomorphicEncryption(keyPair.getPublicKey(), BigInteger.ONE, r);
            CipherText cipherTextProduct = ElGamal.homomorphicAddition(cipherText1, cipherText2);

            int b = ElGamal.homomorphicDecryption(keyPair, cipherTextProduct, maxIterations);

            Assert.assertEquals(2, b);
        } catch (UnableToDecryptException e) {
            fail("Was unable to decrypt encrypted value, with message: " + e.getMessage());
        }
    }

    @Test
    public void testHomomorphicAdditionWhenPlaintextsBothAre0() {
        try {
            KeyPair keyPair = generateKeysFromP11G2();
            BigInteger r = SecurityUtils.getRandomNumModN(keyPair.getPublicKey().getQ());
            CipherText cipherText1 = ElGamal.homomorphicEncryption(keyPair.getPublicKey(), BigInteger.ZERO, r);
            CipherText cipherText2 = ElGamal.homomorphicEncryption(keyPair.getPublicKey(), BigInteger.ZERO, r);
            CipherText cipherTextProduct = ElGamal.homomorphicAddition(cipherText1, cipherText2);

            int b = ElGamal.homomorphicDecryption(keyPair, cipherTextProduct, maxIterations);

            Assert.assertEquals(0, b);
        } catch (UnableToDecryptException e) {
            fail("Was unable to decrypt encrypted value, with message: " + e.getMessage());
        }
    }

    @Test
    public void testHomomorphicAdditionWhenPlaintextsAre0And1() {
        try {
            KeyPair keyPair = generateKeysFromP11G2();
            BigInteger r = SecurityUtils.getRandomNumModN(keyPair.getPublicKey().getQ());
            CipherText cipherText1 = ElGamal.homomorphicEncryption(keyPair.getPublicKey(), BigInteger.ZERO, r);
            CipherText cipherText2 = ElGamal.homomorphicEncryption(keyPair.getPublicKey(), BigInteger.ONE, r);
            CipherText cipherTextProduct = ElGamal.homomorphicAddition(cipherText1, cipherText2);

            int b = ElGamal.homomorphicDecryption(keyPair, cipherTextProduct, maxIterations);

            Assert.assertEquals(1, b);
        } catch (UnableToDecryptException e) {
            fail("Was unable to decrypt encrypted value, with message: " + e.getMessage());
        }
    }

    @Test
    public void testHomomorphicAdditionWhenPlaintextsAre1And0() {
        try {
            KeyPair keyPair = generateKeysFromP11G2();
            BigInteger r = SecurityUtils.getRandomNumModN(keyPair.getPublicKey().getQ());
            CipherText cipherText1 = ElGamal.homomorphicEncryption(keyPair.getPublicKey(), BigInteger.ONE, r);
            CipherText cipherText2 = ElGamal.homomorphicEncryption(keyPair.getPublicKey(), BigInteger.ZERO, r);
            CipherText cipherTextProduct = ElGamal.homomorphicAddition(cipherText1, cipherText2);

            int b = ElGamal.homomorphicDecryption(keyPair, cipherTextProduct, maxIterations);

            Assert.assertEquals(1, b);
        } catch (UnableToDecryptException e) {
            fail("Was unable to decrypt encrypted value, with message: " + e.getMessage());
        }
    }

    @Test
    public void testHomomorphicAdditionWith2048bitP() {
        try {
            KeyPair keyPair = generateKeysFromP2048bitsG2();
            BigInteger r = SecurityUtils.getRandomNumModN(keyPair.getPublicKey().getQ());
            CipherText cipherText1 = ElGamal.homomorphicEncryption(keyPair.getPublicKey(), BigInteger.ONE, r);
            CipherText cipherText2 = ElGamal.homomorphicEncryption(keyPair.getPublicKey(), BigInteger.ZERO, r);
            CipherText cipherTextProduct = ElGamal.homomorphicAddition(cipherText1, cipherText2);

            int b = ElGamal.homomorphicDecryption(keyPair, cipherTextProduct, maxIterations);

            Assert.assertEquals(1, b);
        } catch (UnableToDecryptException e) {
            fail("Was unable to decrypt encrypted value, with message: " + e.getMessage());
        }
    }

    @Test
    public void shouldFailAsCipherTextToBig(){
        KeyPair keyPair = generateKeysFromP2048bitsG2();
        CipherText cipherText = ElGamal.homomorphicEncryption(keyPair.getPublicKey(), BigInteger.valueOf(10));

        try {
            ElGamal.homomorphicDecryption(keyPair, cipherText, 9);
        } catch (UnableToDecryptException e) {
            return;
        }
        fail("Did not throw exception when unable to decrypt");

    }

    public void testPartialDecryption(int message, int excludedIndex) {
        try {
            KeyGenerationParameters params = getKeyGenParamsFromP2048bitsG2();
            DistKeyGenResult distKeyGenResult = ElGamal.generateDistributedKeys(params, 1, 3);

            BigInteger h = SecurityUtils.combinePartials(distKeyGenResult.getPublicValues(), distKeyGenResult.getP());
            PublicKey publicKey = new PublicKey(h, distKeyGenResult.getG(), distKeyGenResult.getQ());
            CipherText cipherText = ElGamal.homomorphicEncryption(publicKey, BigInteger.valueOf(message));

            Map<Integer, BigInteger> partialDecryptions = distKeyGenResult.getSecretValues().entrySet().stream()
                    .filter(e -> e.getKey() != excludedIndex)
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> cipherText.getC().modPow(e.getValue(), distKeyGenResult.getP())
                    ));

            BigInteger combinedPartialDecryptions = SecurityUtils.combinePartials(partialDecryptions, distKeyGenResult.getP());

            int b = ElGamal.homomorphicDecryptionFromPartials(cipherText, combinedPartialDecryptions, distKeyGenResult.getG(), distKeyGenResult.getP(), maxIterations);
            Assert.assertEquals(message, b);
        } catch (UnableToDecryptException e) {
            fail("Was unable to decrypt encrypted value, with message: " + e.getMessage());
        }
    }

    @Test
    public void shouldBeAbleToDecryptPartialsOf7WithDAs123WhenNIs3() {
        testPartialDecryption(7, 0);
    }

    @Test
    public void shouldBeAbleToDecryptPartialsOf857WithDAs123WhenNIs3() {
        testPartialDecryption(857, 0);
    }

    @Test
    public void shouldBeAbleToDecryptPartialsOf7WithDAs12WhenNIs3() {
        testPartialDecryption(7, 3);
    }

    @Test
    public void shouldBeAbleToDecryptPartialsOf857WithDAs12WhenNIs3() {
        testPartialDecryption(857, 3);
    }

    @Test
    public void shouldBeAbleToDecryptPartialsOf7WithDAs13WhenNIs3() {
        testPartialDecryption(7, 2);
    }

    @Test
    public void shouldBeAbleToDecryptPartialsOf857WithDAs13WhenNIs3() {
        testPartialDecryption(857, 2);
    }

    @Test
    public void shouldBeAbleToDecryptPartialsOf7WithDAs23WhenNIs3() {
        testPartialDecryption(7, 1);
    }

    @Test
    public void shouldBeAbleToDecryptPartialsOf857WithDAs23WhenNIs3() {
        testPartialDecryption(857, 1);
    }

    public static class SimpleKeyGenParams implements KeyGenerationParameters {
        private BigInteger g;
        private PrimePair pair;

        SimpleKeyGenParams(BigInteger g, PrimePair pair) {
            this.g = g;
            this.pair = pair;
        }

        @Override
        public PrimePair getPrimePair() {
            return pair;
        }

        @Override
        public BigInteger getGenerator() {
            return g;
        }
    }
}
