package dk.mmj.evhe.entities;

import java.math.BigInteger;

/**
 * Class containing primes p and q
 * where p is a so-called safe prime due to its construction: p = 2q + 1
 */
public class PrimePair {
    private BigInteger p;
    private BigInteger q;

    public PrimePair(BigInteger p, BigInteger q) {
        this.p = p;
        this.q = q;
    }

    public BigInteger getP() {
        return p;
    }

    public BigInteger getQ() {
        return q;
    }
}
