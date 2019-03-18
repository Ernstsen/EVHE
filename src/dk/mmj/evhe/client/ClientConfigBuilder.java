package dk.mmj.evhe.client;

import dk.eSoftware.commandLineParser.CommandLineParser;
import dk.eSoftware.commandLineParser.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

public class ClientConfigBuilder implements CommandLineParser.ConfigBuilder {
    private static final Logger logger = LogManager.getLogger(ClientConfigBuilder.class);
    private static final String SELF = "--client";
    private static final String TARGET_URL = "server=";
    private static final String ID = "id=";
    private static final String VOTE = "vote=";
    private String targetUrl = "https://localhost:8080";
    private String id = "TESTID" + UUID.randomUUID().toString();
    private Boolean vote = null;

    @Override
    public void applyCommand(CommandLineParser.Command command) {
        String cmd = command.getCommand();
        if (cmd.startsWith(TARGET_URL)) {
            targetUrl = cmd.substring(TARGET_URL.length());
        } else if (cmd.startsWith(ID)) {
            id = cmd.substring(ID.length());
        } else if (cmd.startsWith(VOTE)) {
            vote = Boolean.parseBoolean(cmd.substring(VOTE.length()));
        } else if (!cmd.equals(SELF)) {
            logger.warn("Did not recognize command " + command.getCommand());
        }
    }

    @Override
    public Configuration build() {
        return new Client.ClientConfiguration(targetUrl, id, vote);
    }

    @Override
    public String help() {
        return "\tMODE: keyServer\n" +
                "\t  --" + TARGET_URL + "publicServerUrl\t Specifies url for public server to connect to. Standard is: "
                + targetUrl + "\n" +
                "\t  --" + ID + "idString\t id identifying this instance as a unique voter\n" +
                "\t  --" + VOTE + "{true,false} the vote to be cast. If not supplied program will prompt for it\n";
    }
}