package dk.mmj.evhe.crypto.entities;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DistKeyGenResult {
    private BigInteger g;
    private BigInteger q;
    private BigInteger p;
    private List<String> authorityIds;
    private Map<Integer, BigInteger> secretValues;
    private Map<Integer, BigInteger> publicValues;

    /**
     * Unused object mapper constructor
     */
    @SuppressWarnings("unused")
    private DistKeyGenResult() {}

    public DistKeyGenResult(BigInteger g, BigInteger q, Map<Integer, BigInteger> secretValues, Map<Integer, BigInteger> publicValues) {
        this.g = g;
        this.q = q;
        this.p = q.multiply(BigInteger.valueOf(2)).add(BigInteger.ONE);
        this.secretValues = secretValues;
        this.publicValues = publicValues;
        authorityIds = secretValues.keySet().stream()
                .map(Object::toString)
                .collect(Collectors.toList());
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

    public List<String> getAuthorityIds() {
        return authorityIds;
    }

    public Map<Integer, BigInteger> getSecretValues() {
        return secretValues;
    }

    public Map<Integer, BigInteger> getPublicValues() {
        return publicValues;
    }
}
