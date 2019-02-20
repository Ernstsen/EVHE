package dk.mmj.evhe.crypto;

import java.math.BigInteger;

/**
 * Class containing primes p and q
 * where p is a so-called safe prime due to its construction: p = 2q + 1
 */
public class PrimePair {
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
