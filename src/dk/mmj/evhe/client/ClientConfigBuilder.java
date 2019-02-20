package dk.mmj.evhe.client;

import dk.eSoftware.commandLineParser.CommandLineParser;
import dk.eSoftware.commandLineParser.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientConfigBuilder implements CommandLineParser.ConfigBuilder {
    private static final Logger logger = LogManager.getLogger(ClientConfigBuilder.class);
    private static final String SELF = "--client";

    @Override
    public void applyCommand(CommandLineParser.Command command) {
        String cmd = command.getCommand();
        if(!cmd.equals(SELF)){
            logger.warn("Did not recognize command " + command.getCommand());
        }
    }

    @Override
    public Configuration build() {
        return new Client.ClientConfiguration();
    }

    @Override
    public String help() {
        return null;
    }
}
