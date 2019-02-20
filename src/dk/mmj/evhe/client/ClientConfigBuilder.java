package dk.mmj.evhe.client;

import dk.eSoftware.commandLineParser.CommandLineParser;
import dk.eSoftware.commandLineParser.Configuration;

public class ClientConfigBuilder implements CommandLineParser.ConfigBuilder {
    @Override
    public void applyCommand(CommandLineParser.Command command) {

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
