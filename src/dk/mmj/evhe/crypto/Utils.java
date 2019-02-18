package dk.mmj.evhe.crypto;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

/**
 * Class used for methods not tied directly to ElGamal
 */
class Utils {
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
     * @return Primes containing p and q
     */
    static Primes findPrimes(int primeBitLength, int primeCertainty) {
        Random randomBits = new SecureRandom();
        BigInteger q = null;
        BigInteger p = null;

        while (p == null || !p.isProbablePrime(primeCertainty)) {
            q = BigInteger.probablePrime(primeBitLength, randomBits);
            p = q.multiply(new BigInteger("2")).add(BigInteger.ONE);
        }

        return new Primes(p,q);
    }

    /**
     * Finds a suitable generator g for the cyclic group Gq
     *
     * @param primes prime number used in the cyclic group Gq
     * @return generator g for cyclic group Gq
     */
    static BigInteger findGeneratorForGq(Primes primes) {
        BigInteger g = getRandomNumModN(primes.getP());

        BigInteger exponent2 = primes.getP().subtract(BigInteger.ONE).divide(new BigInteger("2"));
        BigInteger exponentQ = primes.getP().subtract(BigInteger.ONE).divide(primes.getQ());

        while (!(g.modPow(exponent2, primes.getP()).compareTo(BigInteger.ONE) == 0) && !(g.modPow(exponentQ, primes.getP()).compareTo(BigInteger.ONE) == 0)) {
            g = getRandomNumModN(primes.getP());
        }

        return g.pow(2);
    }

    /**
     * Class containing primes p and q
     * where p is a so-called safe prime due to its construction: p = 2q + 1
     */
    static class Primes {
        private BigInteger p;
        private BigInteger q;

        Primes(BigInteger p, BigInteger q) {
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
