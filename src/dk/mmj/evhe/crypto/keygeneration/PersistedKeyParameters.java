package dk.mmj.evhe.crypto.keygeneration;

import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;
import dk.mmj.evhe.entities.PrimePair;

import java.math.BigInteger;

import static java.math.BigInteger.ONE;

public class PersistedKeyParameters implements KeyGenerationParameters {
    private PrimePair primePair;
    private BigInteger g;

    /**
     * Generates persisted key parameters from a prime p and a generator g
     *
     * @param pString hexadecimal encoding of prime p
     * @param gString generator g as string
     */
    public PersistedKeyParameters(String pString, String gString) {
        this.g = new BigInteger(gString);

        BigInteger p = new BigInteger(1, HexBin.decode(pString.replaceAll(" ", "")));
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
