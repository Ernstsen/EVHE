package dk.mmj.evhe.client;

import dk.eSoftware.commandLineParser.Configuration;
import dk.mmj.evhe.Application;
import dk.mmj.evhe.crypto.CipherText;
import dk.mmj.evhe.crypto.ElGamal;
import dk.mmj.evhe.crypto.PublicKey;
import dk.mmj.evhe.server.VoteDTO;
import dk.mmj.evhe.server.keyServer.KeyServerConfigBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.JerseyWebTarget;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigInteger;
import java.util.UUID;

public class Client implements Application {
    private static final Logger logger = LogManager.getLogger(KeyServerConfigBuilder.class);
    private JerseyWebTarget target;
    private String id = UUID.randomUUID().toString();

    public Client(ClientConfiguration configuration) {
        JerseyClient client = JerseyClientBuilder.createClient();
        target = client.target(configuration.builder.getTargetUrl());
    }

    @Override
    public void run() {
        assertPublicServer();

        PublicKey publicKey = getPublicKey();
        int vote = getVote();
        CipherText encryptedVote = ElGamal.homomorphicEncryption(publicKey, BigInteger.valueOf(vote));
        postVote(encryptedVote);

    }

    private void assertPublicServer() {
        // Check that we are connected to PublicServer
        Response publicServerResp = target.path("type").request().buildGet().invoke();

        if (publicServerResp.getStatus() != 200) {
            logger.error("Couldn't connect to the publicServer.");
            throw new RuntimeException("Failed : HTTP error code : " + publicServerResp.getStatus());
        }

        String responseEntity = publicServerResp.readEntity(String.class);

        if (!responseEntity.contains("Public Server")) {
            throw new RuntimeException("Server was not of type publicServer");
        }
    }

    private void postVote(CipherText encryptedVote) {
        VoteDTO payload = new VoteDTO(encryptedVote, id);
        Entity<VoteDTO> entity = Entity.entity(payload, MediaType.WILDCARD);

        Response response = target.path("vote").request().post(entity);
        if (response.getStatus() != 200) {
            logger.warn("Failed to post vote to server: Error code was " + response.getStatus());
        }

    }

    private int getVote() {
        return 1;
    }

    private PublicKey getPublicKey() {
        Response response = target.path("publicKey").request().buildGet().invoke();
        return response.readEntity(PublicKey.class);
    }

    public static class ClientConfiguration implements Configuration {
        private ClientConfigBuilder builder;

        ClientConfiguration(ClientConfigBuilder builder) {
            this.builder = builder;
        }

    }
}

