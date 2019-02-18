package dk.mmj.evhe.crypto;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

public class TestUtils {
    @Test
    public void testGenerator() {
        BigInteger p = new BigInteger("11");
        BigInteger q = new BigInteger("5");
        Utils.Primes primes = new Utils.Primes(p, q);
        BigInteger g = Utils.findGeneratorForGq(primes);
        // Proper test is needed here...
        System.out.println("Value of g: " + g.toString());
    }

    @Test
    public void shouldBeAbleToEncryptAndDecrypt0(){
        ElGamal.KeyPair keyPair = ElGamal.generateKeys(3, 50);
        ElGamal.CipherText cipherText = ElGamal.homomorphicEncryption(keyPair.getPublicKey(), new BigInteger("0"));
        BigInteger message = ElGamal.homomorphicDecryption(keyPair, cipherText);

        BigInteger expectedResult = BigInteger.ONE;

        Assert.assertEquals(expectedResult, message);
    }

    @Test
    public void shouldBeAbleToEncryptAndDecrypt1(){
        ElGamal.KeyPair keyPair = ElGamal.generateKeys(3, 50);
        ElGamal.CipherText cipherText = ElGamal.homomorphicEncryption(keyPair.getPublicKey(), BigInteger.ONE);
        BigInteger message = ElGamal.homomorphicDecryption(keyPair, cipherText);

        BigInteger expectedResult = keyPair.getPublicKey().getG();

        Assert.assertEquals(expectedResult, message);
    }
}
