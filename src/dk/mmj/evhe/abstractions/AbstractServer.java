package dk.mmj.evhe.abstractions;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public abstract class AbstractServer implements Application {


    public void run() {
        Server server = getServer(getPort());
        try {
            server.start();
            server.join();
        } catch (Exception e) {
            try {
                e.printStackTrace();
                server.stop();
                server.destroy();
            } catch (Exception e1) {
                e1.printStackTrace();
            }finally {
                server.destroy();
            }
        }
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

        Server jettyServer = new Server(port);
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
