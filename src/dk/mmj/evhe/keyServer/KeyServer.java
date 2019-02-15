package dk.mmj.evhe.keyServer;

import dk.eSoftware.commandLineParser.Configuration;
import dk.mmj.evhe.abstractions.AbstractServer;
import dk.mmj.evhe.abstractions.Application;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class KeyServer extends AbstractServer {
    private int port = 8081;

    public KeyServer(KeyServerConfiguration configuration) {
        if (configuration.port != null) {
            port = configuration.port;
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
        private Integer port;

        KeyServerConfiguration(Integer port) {
            this.port = port;
        }
    }
}
