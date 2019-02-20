package dk.mmj.evhe.crypto;

import org.junit.Test;

import java.math.BigInteger;

public class TestKeyGenerationsParametersImpl {
    @Test
    public void testGenerator() {
        BigInteger p = new BigInteger("11");
        BigInteger q = new BigInteger("5");
        PrimePair primePair = new PrimePair(p, q);
//        BigInteger g = Utils.findGeneratorForGq(primePair);
        // Proper test is needed here...
//        System.out.println("Value of g: " + g.toString());
    }
}
