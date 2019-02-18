package dk.mmj.evhe.crypto;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

public class TestElGamal {
    private ElGamal.KeyPair generateKeysFromP11G2() {
        Utils.PrimePair primes = new Utils.PrimePair(new BigInteger("11"), new BigInteger("5"));
        BigInteger g = new BigInteger("2");
        return ElGamal.generateKeys(primes, g);
    }

    @Test
    public void shouldBeAbleToEncryptAndDecrypt0(){
        ElGamal.KeyPair keyPair = generateKeysFromP11G2();
        ElGamal.CipherText cipherText = ElGamal.homomorphicEncryption(keyPair.getPublicKey(), new BigInteger("0"));
        int b = ElGamal.homomorphicDecryption(keyPair, cipherText);

        Assert.assertEquals(0, b);
    }

    @Test
    public void shouldBeAbleToEncryptAndDecrypt1(){
        ElGamal.KeyPair keyPair = generateKeysFromP11G2();
        ElGamal.CipherText cipherText = ElGamal.homomorphicEncryption(keyPair.getPublicKey(), BigInteger.ONE);
        int b = ElGamal.homomorphicDecryption(keyPair, cipherText);

        Assert.assertEquals(1, b);
    }
}
