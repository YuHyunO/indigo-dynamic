package mb.dnm.dispatcher.http;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletHandler;

@Slf4j
public class HttpDispatcherServer {
    private String rootPath = "/";
    private int port;
    private static HttpDispatcherServer instance;

    public void start() throws Exception {
        if (port == -1) {
            throw new IllegalStateException("Set the server port number");
        }
        Server server = new Server();
        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setPort(port);
        server.addConnector(connector);

        ServletHandler servletHandler = new ServletHandler();
        servletHandler.addServletWithMapping(HttpRequestDispatcher.class, rootPath);
        server.setHandler(servletHandler);

        server.start();
        server.join();
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

    public void setRootPath(String rootPath) {
        if (!rootPath.startsWith("/")) {
            rootPath = "/" + rootPath;
        }

        if (rootPath.length() > 1 && rootPath.endsWith("/")) {
            rootPath = rootPath.substring(0, rootPath.length() - 1);
        }

        if (rootPath.contains("?") || rootPath.contains("&")) {
            throw new IllegalArgumentException("Invalid root path. Illegal characters in root path '?' or '&': " + rootPath);
        }

        this.rootPath = rootPath;
    }

}
