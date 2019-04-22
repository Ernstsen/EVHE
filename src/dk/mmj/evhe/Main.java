package dk.mmj.evhe;

import dk.eSoftware.commandLineParser.CommandLineParser;
import dk.eSoftware.commandLineParser.Configuration;
import dk.eSoftware.commandLineParser.NoSuchBuilderException;
import dk.eSoftware.commandLineParser.WrongFormatException;
import dk.mmj.evhe.client.Client;
import dk.mmj.evhe.client.ClientConfigBuilder;
import dk.mmj.evhe.server.keyServer.DecryptionAuthority;
import dk.mmj.evhe.server.keyServer.DecryptionAuthorityConfigBuilder;
import dk.mmj.evhe.server.bulletinBoard.BulletinBoard;
import dk.mmj.evhe.server.bulletinBoard.BulletinBoardConfigBuilder;

import java.util.HashMap;

public class Main {

    static {
        //Disable JMX as a way to bypass errors known to OpenJDK8
        System.setProperty("log4j2.disable.jmx", Boolean.TRUE.toString());
        //Set loggingManager to be one supplied by Log4J for proper logging
        System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
    }

    public static void main(String[] args) {
        CommandLineParser parser = getParser();
        Configuration configuration;

        if (args.length == 0 || "-h".equals(args[0]) || "-help".equals(args[0])) {
            System.out.println(parser.help());
            return;
        }

        try {
            configuration = parser.parse(args);
        } catch (NoSuchBuilderException e) {
            System.out.println("Failed to match first parameter \"" + args[0] + "\" to a mode of configuration");
            return;
        } catch (WrongFormatException e) {
            System.out.println("One or more parameters were incorrectly formatted: \"" + e.getMessage() + "\"");
            return;
        }


        Application app = getApplication(configuration);
        app.run();
    }

    private static Application getApplication(Configuration parse) {
        if (parse instanceof Client.ClientConfiguration) {
            return new Client((Client.ClientConfiguration) parse);
        } else if (parse instanceof DecryptionAuthority.KeyServerConfiguration) {
            return new DecryptionAuthority((DecryptionAuthority.KeyServerConfiguration) parse);
        } else if (parse instanceof BulletinBoard.BulletinBoardConfiguration) {
            return new BulletinBoard((BulletinBoard.BulletinBoardConfiguration) parse);
        } else {
            System.out.println("" +
                    "====================\n" +
                    "Mapped first parameter to configuration but configuration was not registered. \n" +
                    "This should NEVER happen\n" +
                    "Terminating.\n" +
                    "====================");
            System.exit(-1);
            return null;
        }
    }

    private static CommandLineParser getParser() {
        HashMap<String, CommandLineParser.ConfigBuilder> mapping = new HashMap<>();
        mapping.put("--client", new ClientConfigBuilder());
        mapping.put("--keyServer", new DecryptionAuthorityConfigBuilder());
        mapping.put("--bulletinBoard", new BulletinBoardConfigBuilder());
        return new CommandLineParser(mapping);
    }
}
