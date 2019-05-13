package dk.mmj.evhe.initialization;

import dk.eSoftware.commandLineParser.CommandLineParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

public class TrustedDealerConfigBuilder implements CommandLineParser.ConfigBuilder {
    private static final Logger logger = LogManager.getLogger(TrustedDealerConfigBuilder.class);

    //Configuration options
    private static final String SELF = "--dealer";
    private static final String ROOT_PATH = "root=";
    private static final String KEY_PATH = "keyPath=";
    private static final String SERVERS = "servers=";
    private static final String DEGREE = "degree=";
    private static final String BULLETIN_BOARD_PATH = "url=";
    private static final String NEW_KEY = "newKey=";
    private static final String TIME = "time";
    private static final String TIME_DAY = "day=";
    private static final String TIME_HR = "hour=";
    private static final String TIME_MIN = "min=";


    //state
    private Path rootPath = Paths.get("");
    private Path keyPath = Paths.get("");
    private int servers;
    private int polynomialDegree;
    private String bulletinBoardPath = "https://localhost:8080";
    private boolean newKey = false;
    private long time = 10_000 * 60;


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
        } else if (cmd.startsWith(NEW_KEY)) {
            newKey = Boolean.parseBoolean(cmd.substring(NEW_KEY.length()));
        } else if (cmd.equalsIgnoreCase(TIME)) {
            time = 0;
            for (String param : command.getParams()) {
                int minute = 60 * 1_000;
                int hour = minute * 60;
                int day = hour * 24;

                if (param.startsWith(TIME_DAY)) {
                    time += Integer.parseInt(param.substring(TIME_DAY.length())) * day;
                } else if (param.startsWith(TIME_HR)) {
                    time += Integer.parseInt(param.substring(TIME_HR.length())) * hour;
                } else if (param.startsWith(TIME_MIN)) {
                    time += Integer.parseInt(param.substring(TIME_MIN.length())) * minute;
                }
            }

        } else if (!cmd.equals(SELF)) {
            logger.warn("Did not recognize command " + command.getCommand());
        }
    }

    public TrustedDealer.TrustedDealerConfiguration build() {
        return new TrustedDealer.TrustedDealerConfiguration(
                rootPath,
                keyPath,
                servers,
                polynomialDegree,
                bulletinBoardPath,
                newKey || !keyPathHasKeys(),
                new Date().getTime() + time);
    }

    private boolean keyPathHasKeys() {
        File keyFolder = keyPath.toFile();
        if (!keyFolder.exists()) {
            return false;
        }

        String[] list = keyFolder.list();
        if (list == null) {
            return false;
        }

        boolean pk = false, sk = false;

        for (String s : list) {
            pk |= "rsa.pub".equalsIgnoreCase(s);
            sk |= "rsa".equalsIgnoreCase(s);
        }

        return pk && sk;
    }

    @Override
    public String help() {
        return "\tMODE: Trusted Dealer (dealer) - Trusted dealer creates files to configure Decryption Authorities \n" +
                "\t  --" + ROOT_PATH + "Path\t\t Defines dir to create files in. Relative to curr dir \n" +
                "\t  --" + KEY_PATH + "Path\t Path to the private key used to sign public keys when posting to bb\n" +
                "\t\t -relative \t path is relative to root path instead of current dir\n" +
                "\t  --" + SERVERS + "int\t\t How many servers are going to participate\n" +
                "\t  --" + DEGREE + "int\t\t Degree of polynomial for keygeneration. System is safe when #of corrupt is less or" +
                "equals to degree\n" +
                "\t  --" + BULLETIN_BOARD_PATH + "int\t\t Url pointing to the bulletin board where public keys should be posed\n" +
                "\t  --" + NEW_KEY + "boolean\t Whether new RSA keypair should be generated. If keyPath does not point dir with keys, " +
                "it defaults to true. Otherwise false\n" +
                "\t  --" + TIME + "\t\t Sets time. Vote ends at current time + time parameters. Standard value: 10 min\n" +
                "\t\t -" + TIME_DAY + "days, -" + TIME_HR + "hours, -" + TIME_MIN + "minutes\n";
    }
}