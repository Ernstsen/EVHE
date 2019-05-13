package dk.mmj.evhe.server.decryptionauthority;

import dk.eSoftware.commandLineParser.CommandLineParser;
import dk.eSoftware.commandLineParser.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DecryptionAuthorityConfigBuilder implements CommandLineParser.ConfigBuilder {
    private static final Logger logger = LogManager.getLogger(DecryptionAuthorityConfigBuilder.class);
    private static final String SELF = "--authority";

    //Configuration options
    private static final String PORT = "port=";
    private static final String BULLETIN_BOARD_1 = "bb=";
    private static final String BULLETIN_BOARD_2 = "bulletinBoard=";
    private static final String CONF = "conf=";
    private static final String CORRUPT = "timeCorrupt=";

    //State
    private Integer port;
    private String bulletinBoard = "https://localhost:8080";
    private String confPath = "";
    private Integer timeCorrupt = 0;

    @Override
    public void applyCommand(CommandLineParser.Command command) {
        String cmd = command.getCommand();

        if (cmd.startsWith(PORT)) {
            String intString = cmd.substring(PORT.length());
            port = Integer.parseInt(intString);
        } else if (cmd.startsWith(BULLETIN_BOARD_1)) {
            bulletinBoard = cmd.substring(BULLETIN_BOARD_1.length());
        } else if (cmd.startsWith(BULLETIN_BOARD_2)) {
            bulletinBoard = cmd.substring(BULLETIN_BOARD_2.length());
        } else if (cmd.startsWith(CONF)) {
            confPath = cmd.substring(CONF.length());
        } else if (cmd.startsWith(CORRUPT)) {
            timeCorrupt = Integer.parseInt(cmd.substring(CORRUPT.length()));
        } else if (!cmd.equals(SELF)) {
            logger.warn("Did not recognize command " + command.getCommand());
        }
    }

    @Override
    public Configuration build() {
        return new DecryptionAuthority.DecryptionAuthorityConfiguration(port, bulletinBoard, confPath, timeCorrupt);
    }

    @Override
    public String help() {
        return "" +
                "\tMODE: " + SELF.substring(2) + "\n" +
                "\t  --" + PORT + "int\t\tSpecifies port to be used. Standard=8081\n" +
                "\t  --" + BULLETIN_BOARD_2 + "/" + BULLETIN_BOARD_1 + "ip:port location bulletin board to be used\n" +
                "\t  --" + CONF + "Path\t\tRelative path to config file.\n" +
                "\t  --" + CORRUPT + "int\t\tInteger specifying with what offset a timeCorrupt DA tries to decrypt with.";
    }
}