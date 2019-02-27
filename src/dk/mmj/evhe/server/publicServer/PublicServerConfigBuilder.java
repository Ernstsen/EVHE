package dk.mmj.evhe.server.publicServer;

import dk.eSoftware.commandLineParser.CommandLineParser;
import dk.eSoftware.commandLineParser.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PublicServerConfigBuilder implements CommandLineParser.ConfigBuilder {
    private static final Logger logger = LogManager.getLogger(PublicServerConfigBuilder.class);
    private static final String SELF = "--publicServer";

    @Override
    public void applyCommand(CommandLineParser.Command command) {
        String cmd = command.getCommand();
        if (!cmd.equals(SELF)) {
            logger.warn("Did not recognize command " + command.getCommand());
        }
    }

    @Override
    public Configuration build() {
        return new PublicServer.PublicServerConfiguration(8080);
    }

    @Override
    public String help() {
        return "\tMODE: keyServer\n" +
                "\t Currently not configurable";
    }
}
