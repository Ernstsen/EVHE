package dk.mmj.evhe.server.keyServer;

import dk.eSoftware.commandLineParser.Configuration;
import dk.mmj.evhe.crypto.*;
import dk.mmj.evhe.server.AbstractServer;
import org.eclipse.jetty.servlet.ServletHolder;

import java.math.BigInteger;

public class KeyServer extends AbstractServer {
    private int port = 8081;
    private KeyPair keyPair;

    public KeyServer(KeyServerConfiguration configuration) {
        if (configuration.builder.getPort() != null) {
            port = configuration.builder.getPort();
        }

        if (configuration.hasKeygenParameters()) {
            KeyGenerationParameters params = configuration.builder.getKeygenParams();
            keyPair = ElGamal.generateKeys(params);
        } else {
            KeyGenerationParametersImpl params = new KeyGenerationParametersImpl(1024, 50);
            keyPair = ElGamal.generateKeys(params);
        }
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
