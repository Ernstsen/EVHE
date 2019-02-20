package dk.mmj.evhe.crypto;

import java.math.BigInteger;

import static java.math.BigInteger.ONE;

public class PersistedKeyParameters implements KeyGenerationParameters {
    private PrimePair primePair;
    private BigInteger g;

    public PersistedKeyParameters(String pString, String gString) {
        this.g = new BigInteger(gString);

        BigInteger p = new BigInteger(pString);
        BigInteger q = p.subtract(ONE).divide(BigInteger.valueOf(2));

        this.primePair = new PrimePair(p, q);
    }

    @Override
    public PrimePair getPrimePair() {
        return primePair;
    }

    @Override
    public BigInteger getGenerator() {
        return g;
    }
}
