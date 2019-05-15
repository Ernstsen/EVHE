package dk.mmj.evhe.integrationTest;

import dk.eSoftware.commandLineParser.CommandLineParser;
import dk.eSoftware.commandLineParser.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class IntegrationTestConfigBuilder implements CommandLineParser.ConfigBuilder {
    private static final Logger logger = LogManager.getLogger(IntegrationTestConfigBuilder.class);
    private static final String SELF = "--integrationTest";

    //Configuration options
    private static final String DURATION = "duration=";
    private static final String VOTE = "vote";
    private static final String VOTE_START = "start";
    private static final String VOTE_END = "end";
    private static final String VOTE_AFTER = "after";
    private static final String DISABLED_DECRYPTION_AUTHORITIES = "disabledAuthorities";
    private static final String DA_1 = "1";
    private static final String DA_2 = "2";
    private static final String DA_3 = "3";

    //State
    private int duration = 3;
    private boolean voteStart = false;
    private boolean voteEnd = false;
    private boolean voteAfter = false;
    private boolean decryptionAuthority1 = true;
    private boolean decryptionAuthority2 = true;
    private boolean decryptionAuthority3 = true;

    @Override
    public void applyCommand(CommandLineParser.Command command) {
        String cmd = command.getCommand();

        if (cmd.startsWith(DURATION)) {
            duration = Integer.parseInt(cmd.substring(DURATION.length()));
        } else if (cmd.equals(VOTE)) {
            for (String param : command.getParams()) {
                switch (param) {
                    case VOTE_START:
                        voteStart = true;
                        break;
                    case VOTE_END:
                        voteEnd = true;
                        break;
                    case VOTE_AFTER:
                        voteAfter = true;
                        break;
                    default:
                        logger.warn("Did not recognize voting time \"" + param + "\"");
                }
            }
        } else if (cmd.equals(DISABLED_DECRYPTION_AUTHORITIES)) {
            for (String param : command.getParams()) {
                switch (param) {
                    case DA_1:
                        decryptionAuthority1 = false;
                        break;
                    case DA_2:
                        decryptionAuthority2 = false;
                        break;
                    case DA_3:
                        decryptionAuthority3 = false;
                        break;
                    default:
                        logger.warn("Did not recognize decryption authority \"" + param + "\"");
                }
            }

        } else if (!cmd.equals(SELF)) {
            logger.warn("Did not recognize command: " + cmd);
        }
    }

    @Override
    public Configuration build() {
        List<Integer> decryptionAuthorities = getDecryptionAuthorities();
        List<Integer> voteDelays = getVoteDelays();

        return new IntegrationTest.IntegrationTestConfiguration(decryptionAuthorities, duration, voteDelays);
    }

    private List<Integer> getDecryptionAuthorities() {
        ArrayList<Integer> decryptionAuthorities = new ArrayList<>();
        if (decryptionAuthority1) {
            decryptionAuthorities.add(1);
        }
        if (decryptionAuthority2) {
            decryptionAuthorities.add(2);
        }
        if (decryptionAuthority3) {
            decryptionAuthorities.add(3);
        }

        return decryptionAuthorities;
    }

    private List<Integer> getVoteDelays() {
        ArrayList<Integer> voteDelays = new ArrayList<>();
        if (voteStart) {
            voteDelays.add(0);
        }
        if (voteEnd) {
            voteDelays.add(duration - 15_000);//approx. 15. sec before termination
        }
        if (voteAfter) {
            voteDelays.add(duration + 15_000);//approx. 15. sec after termination
        }

        return voteDelays;
    }

    @Override
    public String help() {
        return "\tMODE: integrationTest\n" +
                "\t  --" + DURATION + "int\t Duration of vote in minutes\n" +
                "\t  --" + VOTE + "\t\t Defines when votes should be cast:\n" +
                "\t\t\t" + VOTE_START + ": after launch, " + VOTE_END + ": aprox. 15 sec. before termination, "
                + VOTE_AFTER + ": aprox. 15 sec after termination\n" +
                "\t  --" + DISABLED_DECRYPTION_AUTHORITIES + "\t Which DA's should not be executed by the automatic test.\n" +
                "\t\t\t " + DA_1 + ", " + DA_2 + " and " + DA_3 + " are the options available";
    }
}
