package dk.mmj.evhe.crypto.keygeneration;

import dk.mmj.evhe.entities.PrimePair;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

import static dk.mmj.evhe.crypto.SecurityUtils.getRandomNumModN;

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
        BigInteger g;
        BigInteger pMinus1 = primePair.getP().subtract(BigInteger.ONE);

        while (true) {
            g = getRandomNumModN(primePair.getP());
            boolean lemma4Condition1 = !g.modPow(pMinus1.divide(primePair.getQ()), primePair.getP()).equals(BigInteger.ONE);
            boolean lemma4Condition2 = !g.modPow(pMinus1.divide(BigInteger.valueOf(2)), primePair.getP()).equals(BigInteger.ONE);

            if (lemma4Condition1 && lemma4Condition2) {
                return g.pow(2);
            }
        }
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
