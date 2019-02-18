package dk.mmj.evhe.keyServer;

import dk.eSoftware.commandLineParser.CommandLineParser;
import dk.eSoftware.commandLineParser.Configuration;
import dk.mmj.evhe.crypto.KeyGenerationParameters;

public class KeyServerConfigBuilder implements CommandLineParser.ConfigBuilder {
    private Integer port;
    private KeyGenerationParameters keygenParams;

    private static final String PORT = "--port=";
    private static final String KEY_PARAMS = "--keyParams=";

    @Override
    public void applyCommand(CommandLineParser.Command command) {
        String cmd = command.getCommand();
        if (cmd.startsWith(PORT)) {
            String intString = cmd.substring(PORT.length());
            port = Integer.parseInt(intString);
        } else if (cmd.startsWith(KEY_PARAMS)) {
            String pathString = cmd.substring(KEY_PARAMS.length());
            loadKeyGenParams(pathString);
        }
    }

    private void loadKeyGenParams(String pathString) {
        //TODO: load from given file and into keygenParams field
    }

    @Override
    public Configuration build() {
        return new KeyServer.KeyServerConfiguration(this);
    }

    Integer getPort() {
        return port;
    }

    KeyGenerationParameters getKeygenParams() {
        return keygenParams;
    }

    @Override
    public String help() {
        return "" +
                "\tMODE: keyServer\n" +
                "\t  --port=portNr\tSpecifies port to be used. Standard=8081\n" +
                "\t  --keyParams=relative/path/to/file\tSpecifies a file with parameters for key-generation\n" +
                "\t\t Only two first lines of file is read. First line must be HEX representation of prime p " +
                " second must be same for generator g";
    }
}
