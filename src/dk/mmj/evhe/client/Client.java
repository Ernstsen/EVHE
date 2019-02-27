package dk.mmj.evhe.client;

import dk.eSoftware.commandLineParser.Configuration;
import dk.mmj.evhe.Application;
import dk.mmj.evhe.server.keyServer.KeyServerConfigBuilder;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.JerseyWebTarget;

import javax.ws.rs.core.Response;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Client implements Application {
    private JerseyWebTarget target;
    private static final Logger logger = LogManager.getLogger(KeyServerConfigBuilder.class);

    public Client(ClientConfiguration configuration) {
        JerseyClient client = JerseyClientBuilder.createClient();
        target = client.target(configuration.builder.getTargetUrl());
    }

    @Override
    public void run() {
        // Check that we are connected to PublicServer
        Response publicServerResp = target.path("type").request().buildGet().invoke();

        if (publicServerResp.getStatus() != 200) {
            logger.error("Couldn't connect to the publicServer.");
            throw new RuntimeException("Failed : HTTP error code : " + publicServerResp.getStatus());
        }

        String responseEntity = publicServerResp.readEntity(String.class);

        System.out.println("Output from publicServer ... \n");
        System.out.println(responseEntity);
    }

    public static class ClientConfiguration implements Configuration {
        private ClientConfigBuilder builder;

        ClientConfiguration(ClientConfigBuilder builder) {
            this.builder = builder;
        }

    }
}

