package dk.mmj.evhe.server.keyServer;

import dk.eSoftware.commandLineParser.CommandLineParser;
import dk.eSoftware.commandLineParser.Configuration;
import dk.mmj.evhe.crypto.keygeneration.KeyGenerationParameters;
import dk.mmj.evhe.crypto.keygeneration.PersistedKeyParameters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class KeyServerConfigBuilder implements CommandLineParser.ConfigBuilder {
    private static final Logger logger = LogManager.getLogger(KeyServerConfigBuilder.class);
    private static final String SELF = "--keyServer";

    //Configuration options
    private static final String PORT = "port=";
    private static final String KEY_PARAMS = "keyParams=";

    //State
    private Integer port;
    private KeyGenerationParameters keygenParams;

    @Override
    public void applyCommand(CommandLineParser.Command command) {
        String cmd = command.getCommand();

        if (cmd.startsWith(PORT)) {
            String intString = cmd.substring(PORT.length());
            port = Integer.parseInt(intString);
        } else if (cmd.startsWith(KEY_PARAMS)) {
            String pathString = cmd.substring(KEY_PARAMS.length());
            loadKeyGenParams(pathString);
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
        return new KeyServer.KeyServerConfiguration(port, keygenParams);
    }

    @Override
    public String help() {
        return "" +
                "\tMODE: keyServer\n" +
                "\t  --port=portNr\tSpecifies port to be used. Standard=8081\n" +
                "\t  --keyParams=relative/path/to/file\tSpecifies a file with parameters for key-generation\n" +
                "\t\t Only two first lines of file is read. First line must be HEX representation of prime p " +
                " second be integer representing generator g";
    }
}