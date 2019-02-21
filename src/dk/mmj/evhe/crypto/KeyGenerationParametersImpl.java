package dk.mmj.evhe.crypto;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

import static dk.mmj.evhe.crypto.Utils.getRandomNumModN;

public class KeyGenerationParametersImpl implements KeyGenerationParameters {
    private BigInteger g;
    private PrimePair primePair;

    public KeyGenerationParametersImpl(int primeBitLength, int primeCertainty) {
        primePair = findPrimes(primeBitLength, primeCertainty);
        g = findGeneratorForGq(primePair);
    }

    /**
     * Finds primes p and q such that p = 2q + 1
     *
     * @param primeBitLength bit length of prime number p
     * @param primeCertainty certainty of p being a prime number (1 - 1/2^certainty)
     * @return PrimePair containing p and q
     */
    private PrimePair findPrimes(int primeBitLength, int primeCertainty) {
        Random randomBits = new SecureRandom();
        BigInteger q = null;
        BigInteger p = null;

        while (p == null || !p.isProbablePrime(primeCertainty)) {
            q = BigInteger.probablePrime(primeBitLength - 1, randomBits);
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
        BigInteger g = null;
        boolean generatorFound = false;

        while (!generatorFound) {
            g = getRandomNumModN(primePair.getP());
            BigInteger i = BigInteger.ONE;

            while (!g.modPow(i, primePair.getP()).equals(BigInteger.ONE) && !generatorFound) {
                if (i.equals(primePair.getP().subtract(BigInteger.valueOf(2)))) {
                    generatorFound = true;
                } else {
                    i = i.add(BigInteger.ONE);
                }
            }
        }
        return g.pow(2);
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
