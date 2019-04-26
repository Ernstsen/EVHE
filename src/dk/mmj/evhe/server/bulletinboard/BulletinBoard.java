package dk.mmj.evhe.server.bulletinboard;


import dk.eSoftware.commandLineParser.Configuration;
import dk.mmj.evhe.entities.CipherText;
import dk.mmj.evhe.entities.PersistedVote;
import dk.mmj.evhe.server.AbstractServer;
import dk.mmj.evhe.server.ServerState;
import org.eclipse.jetty.servlet.ServletHolder;

import java.util.ArrayList;
import java.util.HashSet;


public class BulletinBoard extends AbstractServer {
    static final String PUBLIC_KEY = "publicKey";
    static final String HAS_VOTED = "hasVoted";
    static final String IS_TEST = "isTesting";
    static final String RESULT = "finished";
    static final String VOTES = "votes";

    private BulletinBoardConfiguration configuration;

    public BulletinBoard(BulletinBoardConfiguration configuration) {
        this.configuration = configuration;

        ServerState state = ServerState.getInstance();
        state.put(IS_TEST, configuration.test);
    }

    @Override
    protected void configure(ServletHolder servletHolder) {
        servletHolder.setInitParameter(
                "jersey.config.server.provider.classnames",
                BulletinBoardResource.class.getCanonicalName() + ";" + "org.glassfish.jersey.jackson.JacksonFeature");

        initializeVoting();
    }

    private void initializeVoting() {
        ServerState.getInstance().put(VOTES, new ArrayList<PersistedVote>());
        ServerState.getInstance().put(HAS_VOTED, new HashSet<String>());
    }

    @Override
    protected int getPort() {
        return configuration.port;
    }

    public static class BulletinBoardConfiguration implements Configuration {
        private Integer port;
        private boolean test;

        BulletinBoardConfiguration(Integer port, boolean test) {
            this.port = port;
            this.test = test;
        }
    }
}