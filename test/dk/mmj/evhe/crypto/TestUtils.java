package dk.mmj.evhe.crypto;

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
}
