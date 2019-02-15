package dk.mmj.evhe;

import dk.mmj.evhe.crypto.CryptoTestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)

@Suite.SuiteClasses({
        CryptoTestSuite.class
})
public class TestSuite {
}
