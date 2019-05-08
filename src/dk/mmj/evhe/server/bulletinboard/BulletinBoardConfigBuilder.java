package dk.mmj.evhe.server.bulletinboard;

import dk.eSoftware.commandLineParser.CommandLineParser;
import dk.eSoftware.commandLineParser.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BulletinBoardConfigBuilder implements CommandLineParser.ConfigBuilder {
    private static final Logger logger = LogManager.getLogger(BulletinBoardConfigBuilder.class);

    //Configuration options
    private static final String SELF = "--bulletinBoard";
    private static final String PORT = "port=";

    //State
    private Integer port = 8080;


    @Override
    public void applyCommand(CommandLineParser.Command command) {
        String cmd = command.getCommand();

        if (cmd.startsWith(PORT)) {
            port = Integer.parseInt(cmd.substring(PORT.length()));
        } else if (!cmd.equals(SELF)) {
            logger.warn("Did not recognize command " + command.getCommand());
        }
    }

    @Override
    public Configuration build() {
        return new BulletinBoard.BulletinBoardConfiguration(port);
    }

    @Override
    public String help() {
        return "\tMODE: bulletinBoard\n" +
                "\t  --" + PORT + "int\t\tSpecifies port to be used. Standard=8081\n";
    }
}