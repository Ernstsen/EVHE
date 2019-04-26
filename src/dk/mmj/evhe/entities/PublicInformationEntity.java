package dk.mmj.evhe.entities;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused, JavaDocs")
public class PublicInformationEntity {

    private List<Integer> ids;
    private Map<Integer, BigInteger> publicKeys;
    private BigInteger g;
    private BigInteger q;
    private BigInteger p;

    public PublicInformationEntity() {
    }

    public PublicInformationEntity(List<Integer> ids, Map<Integer, BigInteger> publicKeys, BigInteger g, BigInteger q, BigInteger p) {
        this.ids = ids;
        this.publicKeys = publicKeys;
        this.g = g;
        this.q = q;
        this.p = p;
    }

    public List<Integer> getIds() {
        return ids;
    }

    public void setIds(List<Integer> ids) {
        this.ids = ids;
    }

    public Map<Integer, BigInteger> getPublicKeys() {
        return publicKeys;
    }

    public void setPublicKeys(Map<Integer, BigInteger> publicKeys) {
        this.publicKeys = publicKeys;
    }

    public BigInteger getG() {
        return g;
    }

    public void setG(BigInteger g) {
        this.g = g;
    }

    public BigInteger getQ() {
        return q;
    }

    public void setQ(BigInteger q) {
        this.q = q;
    }

    public BigInteger getP() {
        return p;
    }

    public void setP(BigInteger p) {
        this.p = p;
    }
}
