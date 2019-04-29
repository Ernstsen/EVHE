package dk.mmj.evhe.entities;

import org.bouncycastle.crypto.Signer;

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
    private long endTime;
    private String signature;

    public PublicInformationEntity() {
    }

    public PublicInformationEntity(List<Integer> ids, Map<Integer, BigInteger> publicKeys, BigInteger g, BigInteger q, BigInteger p, long endTime) {
        this.ids = ids;
        this.publicKeys = publicKeys;
        this.g = g;
        this.q = q;
        this.p = p;
        this.endTime = endTime;
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

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    /**
     * Method for reading all public values into a signer, for signature creation or cerification
     *
     * @param signer signer to read info into
     */
    public void updateSigner(Signer signer) {
        for (Integer id : ids) {
            signer.update(id.byteValue());
            byte[] pk = publicKeys.get(id).toByteArray();
            signer.update(pk, 0, pk.length);
        }

        signer.update(g.toByteArray(), 0, g.toByteArray().length);
        signer.update(q.toByteArray(), 0, q.toByteArray().length);
        signer.update(p.toByteArray(), 0, p.toByteArray().length);

        byte[] endTimeBytes = Long.toString(endTime).getBytes();
        signer.update(endTimeBytes, 0, endTimeBytes.length);
    }
}
