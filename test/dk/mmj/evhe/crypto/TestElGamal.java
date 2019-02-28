package dk.mmj.evhe.crypto;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

public class TestElGamal {
    private KeyPair generateKeysFromP11G2() {
        PrimePair primes = new PrimePair(new BigInteger("11"), new BigInteger("5"));
        BigInteger g = new BigInteger("2");
        return ElGamal.generateKeys(new SimpleKeyGenParams(g, primes));
    }

    @Test
    public void shouldBeAbleToEncryptAndDecrypt0() {
        KeyPair keyPair = generateKeysFromP11G2();
        CipherText cipherText = ElGamal.homomorphicEncryption(keyPair.getPublicKey(), new BigInteger("0"));
        int b = ElGamal.homomorphicDecryption(keyPair, cipherText);

        Assert.assertEquals(0, b);
    }

    @Test
    public void shouldBeAbleToEncryptAndDecrypt1() {
        KeyPair keyPair = generateKeysFromP11G2();
        CipherText cipherText = ElGamal.homomorphicEncryption(keyPair.getPublicKey(), BigInteger.ONE);
        int b = ElGamal.homomorphicDecryption(keyPair, cipherText);

        Assert.assertEquals(1, b);
    }

    @Test
    public void testCipherProductsWhenPlainTextsAreBoth1() {
        KeyPair keyPair = generateKeysFromP11G2();
        CipherText cipherText1 = ElGamal.homomorphicEncryption(keyPair.getPublicKey(), BigInteger.ONE);
        CipherText cipherText2 = ElGamal.homomorphicEncryption(keyPair.getPublicKey(), BigInteger.ONE);
        CipherText cipherTextProduct = ElGamal.homomorphicCipherProduct(cipherText1, cipherText2);

        int b = ElGamal.homomorphicDecryption(keyPair, cipherTextProduct);

        Assert.assertEquals(2, b);
    }

    @Test
    public void testCipherProductsWhenPlainTextsAreBoth0() {
        KeyPair keyPair = generateKeysFromP11G2();
        CipherText cipherText1 = ElGamal.homomorphicEncryption(keyPair.getPublicKey(), BigInteger.ZERO);
        CipherText cipherText2 = ElGamal.homomorphicEncryption(keyPair.getPublicKey(), BigInteger.ZERO);
        CipherText cipherTextProduct = ElGamal.homomorphicCipherProduct(cipherText1, cipherText2);

        int b = ElGamal.homomorphicDecryption(keyPair, cipherTextProduct);

        Assert.assertEquals(0, b);
    }

    @Test
    public void testCipherProductsWhenPlainTextsAre0And1() {
        KeyPair keyPair = generateKeysFromP11G2();
        CipherText cipherText1 = ElGamal.homomorphicEncryption(keyPair.getPublicKey(), BigInteger.ZERO);
        CipherText cipherText2 = ElGamal.homomorphicEncryption(keyPair.getPublicKey(), BigInteger.ONE);
        CipherText cipherTextProduct = ElGamal.homomorphicCipherProduct(cipherText1, cipherText2);

        int b = ElGamal.homomorphicDecryption(keyPair, cipherTextProduct);

        Assert.assertEquals(1, b);
    }

    @Test
    public void testCipherProductsWhenPlainTextsAre1And0() {
        KeyPair keyPair = generateKeysFromP11G2();
        CipherText cipherText1 = ElGamal.homomorphicEncryption(keyPair.getPublicKey(), BigInteger.ONE);
        CipherText cipherText2 = ElGamal.homomorphicEncryption(keyPair.getPublicKey(), BigInteger.ZERO);
        CipherText cipherTextProduct = ElGamal.homomorphicCipherProduct(cipherText1, cipherText2);

        int b = ElGamal.homomorphicDecryption(keyPair, cipherTextProduct);

        Assert.assertEquals(1, b);
    }

    private static class SimpleKeyGenParams implements KeyGenerationParameters {
        private BigInteger g;
        private PrimePair pair;

        private SimpleKeyGenParams(BigInteger g, PrimePair pair) {
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
