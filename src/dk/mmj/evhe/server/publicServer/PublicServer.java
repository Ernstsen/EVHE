package dk.mmj.evhe.server.publicServer;


import dk.eSoftware.commandLineParser.Configuration;
import dk.mmj.evhe.crypto.CipherText;
import dk.mmj.evhe.crypto.PublicKey;
import dk.mmj.evhe.server.AbstractServer;
import dk.mmj.evhe.server.ServerState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.JerseyWebTarget;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashSet;


public class PublicServer extends AbstractServer {
    public static final String PUBLIC_KEY = "publicKey";
    static final String VOTES = "votes";
    static final String HAS_VOTED = "hasVoted";
    private static final Logger logger = LogManager.getLogger(PublicServer.class);
    private PublicServerConfiguration configuration;

    public PublicServer(PublicServerConfiguration configuration) {
        this.configuration = configuration;

    }

    @Override
    protected void configure(ServletHolder servletHolder) {
        servletHolder.setInitParameter(
                "jersey.config.server.provider.classnames",
                PublicServerResource.class.getCanonicalName());

        retrievePublicKey();
        initializeVoting();

    }

    private void initializeVoting() {
        ServerState.getInstance().put(VOTES, new ArrayList<CipherText>());
        ServerState.getInstance().put(HAS_VOTED, new HashSet<String>());
    }

    private void retrievePublicKey() {
        JerseyClient client = JerseyClientBuilder.createClient();
        JerseyWebTarget target = client.target(configuration.keyServer);

        Response response = target.path("publicKey").request().buildGet().invoke();

        if (response.getStatus() != 200) {
            RuntimeException e = new RuntimeException("Error when getting Public Key with status " + response.getStatus());
            logger.error("Unable to get PublicKey ", e);
            throw e;
        }

        PublicKey key = response.readEntity(PublicKey.class);

        ServerState.getInstance().put(PUBLIC_KEY, key);
    }

    @Override
    protected int getPort() {
        return configuration.port;
    }

    public static class PublicServerConfiguration implements Configuration {
        private Integer port;
        private String keyServer;

        PublicServerConfiguration(Integer port, String keyServer) {
            this.port = port;
            this.keyServer = keyServer;
        }
    }
}
