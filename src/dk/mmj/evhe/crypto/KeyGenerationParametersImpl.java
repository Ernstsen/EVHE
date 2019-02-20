package dk.mmj.evhe.crypto;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

import static dk.mmj.evhe.crypto.Utils.getRandomNumModN;

public class KeyGenerationParametersImpl implements KeyGenerationParameters {
    private BigInteger g = null;
    private PrimePair primePair = null;

    public KeyGenerationParametersImpl(int primeBitLength, int primeCertainty) {
        primePair = findPrimes(primeBitLength, primeCertainty);
        g = findGeneratorForGq(primePair);
    }

    /**
     * Finds primes p and q such that p = 2q + 1
     *
     * @param primeBitLength bit length of prime number q
     * @param primeCertainty certainty of p being a prime number (1 - 1/2^certainty)
     * @return PrimePair containing p and q
     */
    private PrimePair findPrimes(int primeBitLength, int primeCertainty) {
        Random randomBits = new SecureRandom();
        BigInteger q = null;
        BigInteger p = null;

        while (p == null || !p.isProbablePrime(primeCertainty)) {
            q = BigInteger.probablePrime(primeBitLength, randomBits);
            p = q.multiply(new BigInteger("2")).add(BigInteger.ONE);
        }

        return new PrimePair(p, q);
    }

    /**
     * Finds a suitable generator g for the cyclic group Gq
     *
     * @param primePair prime number used in the cyclic group Gq
     * @return generator g for cyclic group Gq
     */
    private BigInteger findGeneratorForGq(PrimePair primePair) {
        BigInteger g = getRandomNumModN(primePair.getP());

        BigInteger i = BigInteger.ONE;
        while (i.compareTo(primePair.getQ()) < 0) {
            if (g.modPow(i.multiply(new BigInteger("2")), primePair.getQ()).equals(BigInteger.ONE)) {
                g = getRandomNumModN(primePair.getP());
                i = BigInteger.ONE;
            } else {
                i = i.add(BigInteger.ONE);
            }
        }

        return g;
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
