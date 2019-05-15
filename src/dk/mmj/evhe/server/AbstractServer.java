package dk.mmj.evhe.server;

import dk.mmj.evhe.Application;
import dk.mmj.evhe.client.SSLHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;


@SuppressWarnings("WeakerAccess")
public abstract class AbstractServer implements Application {
    private Logger logger = LogManager.getLogger(AbstractServer.class);
    private Server server;

    public void run() {
        int port = getPort();
        logger.info("Starting server on port: " + port);
        server = getServer(port);
        try {
            server.start();
            server.join();
        } catch (Exception e) {
            //noinspection finally
            try {
                logger.warn("Server threw exception while starting or joining:", e);
                server.stop();
                server.destroy();
                System.exit(0);
            } catch (Exception e1) {
                logger.warn("Server threw exception while stopping or destroying:", e1);
            } finally {
                server.destroy();
                logger.info("Server has been destroyed");
                System.exit(0);
            }
        }
    }

    /**
     * Stops and destroys the server
     */
    protected void terminate() {
        logger.info("Terminating server instance");

        try {
            server.stop();
            logger.info("Successfully terminated server");
        } catch (Exception e) {
            logger.warn("Error occurred when stopping server, retrying");
            try {
                server.stop();
            } catch (Exception e1) {
                logger.warn("Error occurred during second attempt at stopping server", e);
            }
        } finally {
            server.destroy();
        }

        System.exit(0);
    }

    /**
     * Generates and returns a {@link Server}
     * <br/>
     * After creation the method <code>configure</code> method is called
     * with the servletHolder as a parameter, so <code>initParameters</code> can be set.
     *
     * @param port for which the server should listen
     * @return a {@link Server}
     */
    protected Server getServer(int port) {
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        ServletHolder jerseyServlet = context.addServlet(
                org.glassfish.jersey.servlet.ServletContainer.class, "/*");
        jerseyServlet.setInitOrder(0);

        Server jettyServer = new Server();

        HttpConfiguration https = new HttpConfiguration();
        https.addCustomizer(new SecureRequestCustomizer());

        SslContextFactory sslContextFactory = SSLHelper.getSSLContextFactory();

        ServerConnector sslConnector = new ServerConnector(jettyServer,
                new SslConnectionFactory(sslContextFactory, "http/1.1"),
                new HttpConnectionFactory(https));
        sslConnector.setPort(port);

        jettyServer.setConnectors(new Connector[]{sslConnector});
        jettyServer.setHandler(context);

        configure(jerseyServlet);

        return jettyServer;
    }

    /**
     * Method that is used to configure the {@link ServletHolder} that is used
     * in the {@link Server} returned by the <code>getServer</code> method call
     * <br/><br/>
     * This is where servlets are registered
     *
     * @param servletHolder servlet to be configured
     */
    protected abstract void configure(ServletHolder servletHolder);

    /**
     * Method for informing about what port to listen to
     *
     * @return port for the server
     */
    protected abstract int getPort();

}