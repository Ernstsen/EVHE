package dk.mmj.evhe.server.keyServer;

import dk.eSoftware.commandLineParser.Configuration;
import dk.mmj.evhe.crypto.ElGamal;
import dk.mmj.evhe.crypto.keygeneration.KeyGenerationParameters;
import dk.mmj.evhe.crypto.keygeneration.KeyGenerationParametersImpl;
import dk.mmj.evhe.crypto.entities.KeyPair;
import dk.mmj.evhe.server.AbstractServer;
import dk.mmj.evhe.server.ServerState;
import org.eclipse.jetty.servlet.ServletHolder;

public class KeyServer extends AbstractServer {
    static final String KEY_PAIR = "keypair";
    private int port = 8081;

    public KeyServer(KeyServerConfiguration configuration) {
        if (configuration.port != null) {
            port = configuration.port;
        }

        KeyPair keyPair;

        if (configuration.keygenParams != null) {
            KeyGenerationParameters params = configuration.keygenParams;
            keyPair = ElGamal.generateKeys(params);
        } else {
            KeyGenerationParametersImpl params = new KeyGenerationParametersImpl(1024, 50);
            keyPair = ElGamal.generateKeys(params);
        }

        ServerState.getInstance().put(KEY_PAIR, keyPair);
    }


    @Override
    protected void configure(ServletHolder servletHolder) {
        servletHolder.setInitParameter(
                "jersey.config.server.provider.classnames",
                KeyServerResource.class.getCanonicalName());
    }

    @Override
    protected int getPort() {
        return port;
    }

    /**
     * Configuration for a KeyServer
     */
    public static class KeyServerConfiguration implements Configuration {
        private final KeyGenerationParameters keygenParams;
        private final Integer port;

        KeyServerConfiguration(Integer port, KeyGenerationParameters keygenParams) {
            this.port = port;
            this.keygenParams = keygenParams;
        }

    }
}