package dk.mmj.evhe.crypto;

import java.math.BigInteger;

/**
 * Class used for methods not tied directly to ElGamal
 */
class Utils {
    /**
     * Finds a suitable generator g for the cyclic group Gq
     *
     * @param q prime number used in the cyclic group Gq
     * @return generator g for cyclic group Gq
     * @throws NoGeneratorFoundException when no suitable generator g is found
     */
    static BigInteger findGeneratorForGq(BigInteger q) throws NoGeneratorFoundException {
        BigInteger g = new BigInteger("2");

        // While the prime number q is greater than the potential generator g
        while (q.compareTo(g) == 1) {
            if (q.gcd(g).equals(BigInteger.ONE)) {
                return g;
            }
            g = g.add(BigInteger.ONE);
        }

        throw new NoGeneratorFoundException("No generator g for cyclic group Gq was found");
    }
}
