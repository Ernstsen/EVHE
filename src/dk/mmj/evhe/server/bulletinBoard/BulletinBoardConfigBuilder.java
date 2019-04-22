package dk.mmj.evhe.server.bulletinBoard;

import dk.eSoftware.commandLineParser.CommandLineParser;
import dk.eSoftware.commandLineParser.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BulletinBoardConfigBuilder implements CommandLineParser.ConfigBuilder {
    private static final Logger logger = LogManager.getLogger(BulletinBoardConfigBuilder.class);
    private static final String KEY_SERVER = "keyServer=";

    //Configuration options
    private static final String SELF = "--bulletinBoard";
    private static final String TEST = "test=";

    //State
    private boolean test = false;
    private String keyServer = "https://localhost:8081";


    @Override
    public void applyCommand(CommandLineParser.Command command) {
        String cmd = command.getCommand();

        if (cmd.startsWith(KEY_SERVER)) {
            keyServer = cmd.substring(KEY_SERVER.length());
        } else if (cmd.startsWith(TEST)) {
            test = Boolean.parseBoolean(cmd.substring(TEST.length()));
        } else if (!cmd.equals(SELF)) {
            logger.warn("Did not recognize command " + command.getCommand());
        }
    }

    @Override
    public Configuration build() {
        return new BulletinBoard.BulletinBoardConfiguration(8080, test);
    }

    @Override
    public String help() {
        return "\tMODE: bulletinBoard\n" +
                "\t  --" + TEST + "{true,false}\t specifies whether ids is allowed to be testing ids - not pre-determined ones";
    }
}