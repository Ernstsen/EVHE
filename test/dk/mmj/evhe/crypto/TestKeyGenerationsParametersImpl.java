package dk.mmj.evhe.crypto;

import org.junit.Assert;
import org.junit.Test;

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
}
