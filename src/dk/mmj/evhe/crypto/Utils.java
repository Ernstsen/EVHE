package dk.mmj.evhe.crypto;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

/**
 * Class used for methods not tied directly to ElGamal
 */
class Utils {
    /**
     * Find a random number in the range [1;n)
     *
     * @param n n-1 is upper limit in interval
     * @return random number in range [1;n)
     */
    static BigInteger getRandomNumModN(BigInteger n) {
        Random randomBits = new SecureRandom();
        BigInteger result = null;

        while (result == null || result.compareTo(new BigInteger("0")) == 0) {
            result = new BigInteger(n.bitLength(), randomBits).mod(n);
        }

        return result;
    }
}
