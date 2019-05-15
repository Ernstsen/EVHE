package dk.mmj.evhe.entities;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DistKeyGenResult {
    private BigInteger g;
    private BigInteger q;
    private BigInteger p;
    private List<Integer> authorityIds;
    private Map<Integer, BigInteger> secretValues;
    private Map<Integer, BigInteger> publicValues;

    /**
     * Unused object mapper constructor
     */
    @SuppressWarnings("unused")
    private DistKeyGenResult() {
    }

    public DistKeyGenResult(BigInteger g, BigInteger q, Map<Integer, BigInteger> secretValues, Map<Integer, BigInteger> publicValues) {
        this.g = g;
        this.q = q;
        this.p = q.multiply(BigInteger.valueOf(2)).add(BigInteger.ONE);
        this.secretValues = secretValues;
        this.publicValues = publicValues;
        authorityIds = new ArrayList<>(secretValues.keySet());
    }

    public BigInteger getG() {
        return g;
    }

    public BigInteger getQ() {
        return q;
    }

    public BigInteger getP() {
        return p;
    }

    public List<Integer> getAuthorityIds() {
        return authorityIds;
    }

    public Map<Integer, BigInteger> getSecretValues() {
        return secretValues;
    }

    public Map<Integer, BigInteger> getPublicValues() {
        return publicValues;
    }
}
