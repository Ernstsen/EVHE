package dk.mmj.evhe.crypto.keygeneration;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TestKeyGenerationsParametersImpl {

    /**
     * Tests if generator found for P of length 4 bits is correct
     * With length of 4 bits, it can only be that p = 2*q + 1 = 2*5 + 1 = 11
     */
    @Test
    public void testGeneratorForPBitLength4() {
        KeyGenerationParameters params = new KeyGenerationParametersImpl(4, 50);
        List<Integer> possibleGeneratorsForP = Arrays.asList(2, 6, 7, 8);
        List<Integer> possibleGeneratorsForQ = possibleGeneratorsForP.stream().map(i -> (int) Math.pow(i.doubleValue(), 2.0)).collect(Collectors.toList());
        Assert.assertTrue(possibleGeneratorsForQ.contains(params.getGenerator().intValue()));
    }

    /**
     * Tests if generator found for P of length 5 bits is correct
     * With length of 5 bits, it can only be that p = 2*q + 1 = 2*11 + 1 = 23
     */
    @Test
    public void testGeneratorForPBitLength5() {
        KeyGenerationParameters params = new KeyGenerationParametersImpl(5, 50);
        List<Integer> possibleGeneratorsForP = Arrays.asList(5, 7, 10, 11, 14, 15, 17, 19, 20, 21);
        List<Integer> possibleGeneratorsForQ = possibleGeneratorsForP.stream().map(i -> (int) Math.pow(i.doubleValue(), 2.0)).collect(Collectors.toList());
        Assert.assertTrue(possibleGeneratorsForQ.contains(params.getGenerator().intValue()));
    }

    /**
     * Tests if generator found for P of length 6 bits is correct
     * With length of 6 bits, it can be that p = 2*q + 1 = 2*23 + 1 = 47 or p = 2*q + 1 = 2*29 + 1 = 59
     */
    @Test
    public void testGeneratorForPBitLength6() {
        KeyGenerationParameters params = new KeyGenerationParametersImpl(6, 50);
        List<Integer> possibleGeneratorsForP;

        if (params.getPrimePair().getP().equals(BigInteger.valueOf(47))) {
            possibleGeneratorsForP = Arrays.asList(5, 10, 11, 13, 15, 19, 20, 22, 23, 26, 29, 30, 31, 33, 35, 38, 39, 40, 41, 43, 44, 45);
        } else {
            possibleGeneratorsForP = Arrays.asList(2, 6, 8, 10, 11, 13, 14, 18, 23, 24, 30, 31, 32, 33, 34, 37, 38, 39, 40, 42, 43, 44, 47, 50, 52, 54, 55, 56);
        }
        List<Integer> possibleGeneratorsForQ = possibleGeneratorsForP.stream().map(i -> (int) Math.pow(i.doubleValue(), 2.0)).collect(Collectors.toList());
        Assert.assertTrue(possibleGeneratorsForQ.contains(params.getGenerator().intValue()));
    }
}
