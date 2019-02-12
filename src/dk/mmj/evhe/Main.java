package dk.mmj.evhe;

import dk.eSoftware.commandLineParser.CommandLineParser;
import dk.eSoftware.commandLineParser.Configuration;
import dk.eSoftware.commandLineParser.NoSuchBuilderException;
import dk.eSoftware.commandLineParser.WrongFormatException;
import dk.mmj.evhe.client.Client;
import dk.mmj.evhe.client.ClientConfigBuilder;
import dk.mmj.evhe.keyServer.KeyServer;
import dk.mmj.evhe.keyServer.KeyServerConfigBuilder;
import dk.mmj.evhe.publicServer.PublicServer;
import dk.mmj.evhe.publicServer.PublicServerConfigBuilder;

import java.util.HashMap;

public class Main {

    public static void main(String[] args) {
        CommandLineParser parser = getParser();
        Configuration parse;

        if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
            System.out.println(parser.help());
            return;
        }

        try {
            parse = parser.parse(args);
        } catch (NoSuchBuilderException e) {
            System.out.printf("Failed to match first parameter \"" + args[0] + "\" to a mode of configuration");
            return;
        } catch (WrongFormatException e) {
            System.out.println("One or more parameters were incorrectly formatted: \"" + e.getMessage() + "\"");
            return;
        }


        Application app;
        if (parse instanceof Client.ClientConfiguration) {
            app = new Client((Client.ClientConfiguration) parse);
        } else if (parse instanceof KeyServer.KeyServerConfiguration) {
            app = new KeyServer((KeyServer.KeyServerConfiguration) parse);
        } else if (parse instanceof PublicServer.PublicServerConfiguration) {
            app = new PublicServer((PublicServer.PublicServerConfiguration) parse);
        } else {
            System.out.println("" +
                    "====================\n" +
                    "Mapped first parameter to configuration but configuration was not registered. \n" +
                    "This should NEVER happen\n" +
                    "Terminating.\n" +
                    "====================");
            return;
        }
        app.run();
    }

    private static CommandLineParser getParser() {
        HashMap<String, CommandLineParser.ConfigBuilder> mapping = new HashMap<>();
        mapping.put("client", new ClientConfigBuilder());
        mapping.put("keyServer", new KeyServerConfigBuilder());
        mapping.put("publicServer", new PublicServerConfigBuilder());
        return new CommandLineParser(mapping);
    }
}
