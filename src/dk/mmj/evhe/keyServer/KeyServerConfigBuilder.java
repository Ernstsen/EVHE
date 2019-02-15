package dk.mmj.evhe.keyServer;

import dk.eSoftware.commandLineParser.CommandLineParser;
import dk.eSoftware.commandLineParser.Configuration;

public class KeyServerConfigBuilder implements CommandLineParser.ConfigBuilder {
    @Override
    public void applyCommand(CommandLineParser.Command command) {

    }

    @Override
    public Configuration build() {
        return new KeyServer.KeyServerConfiguration(8081);
    }

    @Override
    public String help() {
        return null;
    }
}
