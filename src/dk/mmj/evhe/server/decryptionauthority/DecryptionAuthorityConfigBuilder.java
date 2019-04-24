package dk.mmj.evhe.server.decryptionauthority;

import dk.eSoftware.commandLineParser.CommandLineParser;
import dk.eSoftware.commandLineParser.Configuration;
import dk.mmj.evhe.crypto.keygeneration.KeyGenerationParameters;
import dk.mmj.evhe.crypto.keygeneration.PersistedKeyParameters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class DecryptionAuthorityConfigBuilder implements CommandLineParser.ConfigBuilder {
    private static final Logger logger = LogManager.getLogger(DecryptionAuthorityConfigBuilder.class);
    private static final String SELF = "--keyServer";

    //Configuration options
    private static final String PORT = "port=";
    private static final String KEY_PARAMS = "keyParams=";
    private static final String BULLETIN_BOARD_1 = "bb=";
    private static final String BULLETIN_BOARD_2 = "bulletinBoard=";

    //State
    private Integer port;
    private KeyGenerationParameters keygenParams;
    private String bulletinBoard = "https://localhost:8080";

    @Override
    public void applyCommand(CommandLineParser.Command command) {
        String cmd = command.getCommand();

        if (cmd.startsWith(PORT)) {
            String intString = cmd.substring(PORT.length());
            port = Integer.parseInt(intString);
        } else if (cmd.startsWith(KEY_PARAMS)) {
            String pathString = cmd.substring(KEY_PARAMS.length());
            loadKeyGenParams(pathString);
        } else if (cmd.startsWith(BULLETIN_BOARD_1)) {
            bulletinBoard = cmd.substring(BULLETIN_BOARD_1.length());
        } else if (cmd.startsWith(BULLETIN_BOARD_2)) {
            bulletinBoard = cmd.substring(BULLETIN_BOARD_2.length());
        } else if (!cmd.equals(SELF)) {
            logger.warn("Did not recognize command " + command.getCommand());
        }
    }

    /**
     * Loads parameters for keygeneration from a file into a {@link PersistedKeyParameters} pbject
     *
     * @param pathString path to the file
     */
    private void loadKeyGenParams(String pathString) {
        logger.info("Parsing parameters for key generation");

        try (BufferedReader reader = new BufferedReader(new FileReader(pathString))) {
            String pHex = reader.readLine();
            String gHex = reader.readLine();

            keygenParams = new PersistedKeyParameters(pHex, gHex);

        } catch (IOException e) {
            logger.error("Unable to load key generation parameters from file", e);
            throw new RuntimeException(e);
        }

        logger.info("Successfully read parameters for key generation from file ");
    }

    @Override
    public Configuration build() {
        return new DecryptionAuthority.KeyServerConfiguration(port, keygenParams, bulletinBoard);
    }

    @Override
    public String help() {
        return "" +
                "\tMODE: keyServer\n" +
                "\t  --port=int\t\tSpecifies port to be used. Standard=8081\n" +
                "\t  --keyParams=rPath\tSpecifies a file with parameters for key-generation. Path is relative to current dir\n" +
                "\t\t Only two first lines of file is read. First line must be HEX representation of prime p " +
                " second must be integer representing generator g\n" +
                "\t  --bulletinBoard/bb=ip:port location bulletin board to be used\n";
    }
}