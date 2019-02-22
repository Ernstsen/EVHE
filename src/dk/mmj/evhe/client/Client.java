package dk.mmj.evhe.client;

import dk.eSoftware.commandLineParser.Configuration;
import dk.mmj.evhe.Application;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.JerseyWebTarget;

import javax.ws.rs.core.Response;

public class Client implements Application {
    private final JerseyWebTarget target;


    public Client(ClientConfiguration configuration) {

        JerseyClient client = JerseyClientBuilder.createClient();
        target = client.target(configuration.targetUrl);


    }

    @Override
    public void run() {
        Response somePath1 = target.path("somePath").request().buildGet().invoke();
    }

    public static class ClientConfiguration implements Configuration {
        private String targetUrl;

        public ClientConfiguration(String targetUrl) {
            this.targetUrl = targetUrl;
        }
    }
}

