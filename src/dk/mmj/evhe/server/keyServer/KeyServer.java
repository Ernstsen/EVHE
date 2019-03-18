package dk.mmj.evhe.server.keyServer;

import dk.eSoftware.commandLineParser.Configuration;
import dk.mmj.evhe.crypto.ElGamal;
import dk.mmj.evhe.crypto.KeyGenerationParameters;
import dk.mmj.evhe.crypto.KeyGenerationParametersImpl;
import dk.mmj.evhe.crypto.KeyPair;
import dk.mmj.evhe.server.AbstractServer;
import dk.mmj.evhe.server.ServerState;
import org.eclipse.jetty.servlet.ServletHolder;

public class KeyServer extends AbstractServer {
    static final String KEY_PAIR = "keypair";
    private int port = 8081;

    public KeyServer(KeyServerConfiguration configuration) {
        if (configuration.builder.getPort() != null) {
            port = configuration.builder.getPort();
        }

        KeyPair keyPair;
        if (configuration.hasKeygenParameters()) {
            KeyGenerationParameters params = configuration.builder.getKeygenParams();
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

    public static class KeyServerConfiguration implements Configuration {
        private KeyServerConfigBuilder builder;

        KeyServerConfiguration(KeyServerConfigBuilder builder) {
            this.builder = builder;
        }

        private boolean hasKeygenParameters() {
            return builder.getKeygenParams() != null;
        }
    }
}