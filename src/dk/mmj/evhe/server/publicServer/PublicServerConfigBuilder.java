package dk.mmj.evhe.server.publicServer;

import dk.eSoftware.commandLineParser.CommandLineParser;
import dk.eSoftware.commandLineParser.Configuration;

public class PublicServerConfigBuilder implements CommandLineParser.ConfigBuilder {

    @Override
    public void applyCommand(CommandLineParser.Command command) {

    }

    @Override
    public Configuration build() {
        return new PublicServer.PublicServerConfiguration(8080);
    }

    @Override
    public String help() {
        return null;
    }
}
