package dk.mmj.evhe.crypto;

import dk.mmj.evhe.crypto.keygeneration.TestKeyGenerationsParametersImpl;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)

@Suite.SuiteClasses({
        TestKeyGenerationsParametersImpl.class,
        TestElGamal.class,
        TestVoteProofUtils.class,
        TestSecurityUtils.class
})
public class CryptoTestSuite {
}
