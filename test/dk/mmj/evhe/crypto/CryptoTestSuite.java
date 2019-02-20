package dk.mmj.evhe.crypto;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)

@Suite.SuiteClasses({
        TestKeyGenerationsParametersImpl.class,
        TestElGamal.class
})
public class CryptoTestSuite {
}
