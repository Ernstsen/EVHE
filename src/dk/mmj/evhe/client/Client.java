package dk.mmj.evhe.client;

import dk.eSoftware.commandLineParser.Configuration;
import dk.mmj.evhe.abstractions.Application;

public class Client implements Application {

    public Client(ClientConfiguration configuration) {
    }

    @Override
    public void run() {

    }

    public static class ClientConfiguration implements Configuration {

    }
}
