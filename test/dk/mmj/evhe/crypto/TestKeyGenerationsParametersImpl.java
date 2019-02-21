package dk.mmj.evhe.crypto;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class TestKeyGenerationsParametersImpl {
    @Test
    public void testGeneratorForPBitLength4() {
        // With 4 bits it can only be that p = 2*q +1 = 2*5 + 1 = 11
        KeyGenerationParameters params = new KeyGenerationParametersImpl(4, 50);
        List<Integer> possibleGeneratorsForP = Arrays.asList(4,36,49,72);
        Assert.assertTrue(possibleGeneratorsForP.contains(params.getGenerator().intValue()));
    }
}
