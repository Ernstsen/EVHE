package dk.mmj.evhe.crypto.keygeneration;

import dk.mmj.evhe.crypto.entities.PrimePair;

import java.math.BigInteger;

public interface KeyGenerationParameters {

    /**
     * Getter for the pair q,p used in ElGamal key-generation
     *
     * @return a {@link PrimePair} with (p,q)
     */
    PrimePair getPrimePair();

    /**
     * returns generator <code>g</code> for the group
     *
     * @return {@link BigInteger} representing g
     */
    BigInteger getGenerator();


}
