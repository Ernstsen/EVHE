package dk.mmj.evhe.crypto;

import java.math.BigInteger;

public interface KeyGenerationParameters {

    /**
     * Getter for the pair q,p used in ElGamal key-generation
     *
     * @return a {@link dk.mmj.evhe.crypto.PrimePair} with (p,q)
     */
    PrimePair getPrimePair();

    /**
     * returns generator <code>g</code> for the group
     *
     * @return {@link BigInteger} representing g
     */
    BigInteger getGenerator();


}
