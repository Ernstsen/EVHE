package dk.mmj.evhe.initialization;

import dk.eSoftware.commandLineParser.CommandLineParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;

public class TrustedDealerConfigBuilder implements CommandLineParser.ConfigBuilder {
    private static final Logger logger = LogManager.getLogger(TrustedDealerConfigBuilder.class);

    //Configuration options
    private static final String SELF = "dealer";
    private static final String ROOT_PATH = "root=";
    private static final String KEY_PATH = "key=";
    private static final String SERVERS = "servers=";
    private static final String DEGREE = "degree=";
    private static final String BULLETIN_BOARD_PATH = "url=";


    //state
    private Path rootPath = Paths.get("");
    private Path keyPath = Paths.get("id_rsa");
    private int servers;
    private int polynomialDegree;
    private String bulletinBoardPath = "https://localhost:8080";


    @Override
    public void applyCommand(CommandLineParser.Command command) {
        String cmd = command.getCommand();
        if (cmd.startsWith(ROOT_PATH)) {
            rootPath = Paths.get(cmd.substring(ROOT_PATH.length()));
        } else if (cmd.startsWith(KEY_PATH)) {
            String path = cmd.substring(KEY_PATH.length());

            if (command.getParams().stream().anyMatch(s -> s.contains("relative"))) {
                keyPath = rootPath.resolve(path);
            } else {
                keyPath = Paths.get(path);
            }
        } else if (cmd.startsWith(SERVERS)) {
            servers = Integer.parseInt(cmd.substring(SERVERS.length()));
        } else if (cmd.startsWith(DEGREE)) {
            polynomialDegree = Integer.parseInt(cmd.substring(DEGREE.length()));
        } else if (cmd.startsWith(BULLETIN_BOARD_PATH)) {
            bulletinBoardPath = cmd.substring(BULLETIN_BOARD_PATH.length());
        } else if (!cmd.equals(SELF)) {
            logger.warn("Did not recognize command " + command.getCommand());
        }


    }

    public TrustedDealer.TrustedDealerConfiguration build() {
        return new TrustedDealer.TrustedDealerConfiguration(rootPath, keyPath, servers, polynomialDegree, bulletinBoardPath);
    }

    @Override
    public String help() {
        return "\tMODE: Trusted Dealer (dealer) - Trusted dealer creates files to configure Decryption Authorities \n" +
                "\t  --" + ROOT_PATH + "Path\t\t Defines dir to create files in. Relative to curr dir \n" +
                "\t  --" + KEY_PATH + "Path\t\t Path to the private key used to sign public keys when posting to bb\n" +
                "\t\t -relative \t path is relative to root path instead of current dir\n" +
                "\t  --" + SERVERS + "int\t\t How many servers are going to participate\n" +
                "\t  --" + DEGREE + "int\t\t Degree of polynomial for keygeneration. System is safe when #of corrupt is less or" +
                "equals to degree\n" +
                "\t  --" + BULLETIN_BOARD_PATH + "int\t\t Url pointing to the bulletin board where public keys should be posed\n";
    }
}