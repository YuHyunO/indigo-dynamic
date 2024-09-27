package mb.dnm.dispatcher.http;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletHandler;

@Slf4j
public class HttpDispatcherServer {
    private final String ROOT_PATH = "/";
    private int port;
    private static HttpDispatcherServer instance;

    public void start() throws Exception {
        if (port == -1) {
            throw new IllegalStateException("Set the server port number");
        }
        final Server server = new Server();
        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setPort(port);
        server.addConnector(connector);

        ServletHandler servletHandler = new ServletHandler();
        servletHandler.addServletWithMapping(HttpRequestDispatcher.class, ROOT_PATH);
        server.setHandler(servletHandler);
        log.debug("The root path is '{}'", ROOT_PATH);

        Thread bootThread = null;
        bootThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    server.start();
                    server.join();
                } catch (Exception e) {
                    log.error("A fatal error occurred when stating HttpDispatcherServer", e);
                    System.exit(-1);
                }
            }
        });
        bootThread.setName("BootThread-HttpDispatcherServer");
        log.info("Starting boot thread for HttpDispatcherServer. Thread name: {}", bootThread.getName());
        bootThread.start();
    }


    public void setPort(int port) {
        if ((port >= 0 && port <= 1023)) {
            throw new IllegalArgumentException("You can not use a port between 0 and 1023 (well-known port)");
        } else if (port >= 1024 && port <= 65535) {
            this.port = port;
        } else {
            throw new IllegalArgumentException("Invalid port number: " + port);
        }
    }


}
