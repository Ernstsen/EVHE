package dk.mmj.evhe.publicServer;


import dk.eSoftware.commandLineParser.Configuration;
import dk.mmj.evhe.abstractions.AbstractServer;
import dk.mmj.evhe.abstractions.Application;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class PublicServer extends AbstractServer {
    private int port = 8080;

    public PublicServer(PublicServerConfiguration configuration) {
        if (configuration.port != null) {
            port = configuration.port;
        }

    }

    @Override
    protected void configure(ServletHolder servletHolder) {
        servletHolder.setInitParameter(
                "jersey.config.server.provider.classnames",
                PublicServerResource.class.getCanonicalName());

    }

    @Override
    protected int getPort() {
        return port;
    }

    public static class PublicServerConfiguration implements Configuration {
        private Integer port;

        PublicServerConfiguration(Integer port) {
            this.port = port;
        }
    }
}
