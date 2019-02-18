package dk.mmj.evhe.crypto;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

/**
 * Class used for methods not tied directly to ElGamal
 */
public class Utils {
    /**
     * Find a random number in the range [1;n-1]
     *
     * @param n n-1 is upper limit in interval
     * @return random number in range [1;n-1]
     */
    static BigInteger getRandomNumModN(BigInteger n) {
        Random randomBits = new SecureRandom();
        BigInteger result = null;

        while (result == null || result.compareTo(new BigInteger("0")) == 0) {
            result = new BigInteger(n.bitLength(), randomBits).mod(n);
        }

        return result;
    }

    /**
     * Finds primes p and q such that p = 2q + 1
     *
     * @param primeBitLength bit length of prime number q
     * @param primeCertainty certainty of p being a prime number (1 - 1/2^certainty)
     * @return PrimePair containing p and q
     */
    public static PrimePair findPrimes(int primeBitLength, int primeCertainty) {
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
    public static BigInteger findGeneratorForGq(PrimePair primePair) {
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

    /**
     * Class containing primes p and q
     * where p is a so-called safe prime due to its construction: p = 2q + 1
     */
    public static class PrimePair {
        private BigInteger p;
        private BigInteger q;

        PrimePair(BigInteger p, BigInteger q) {
            this.p = p;
            this.q = q;
        }

        BigInteger getP() {
            return p;
        }

        BigInteger getQ() {
            return q;
        }
    }
}
