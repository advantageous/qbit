package io.advantageous.qbit.servlet.websocketproto.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.jsr356.server.ServerContainer;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;

public class ServerMain  {
    public static void main( String... args ) throws Exception {
        final Server server = new Server(8080);
        final ServletContextHandler context = new ServletContextHandler();

        server.setHandler(context);
        final ServerContainer serverContainer = WebSocketServerContainerInitializer.configureContext(context);
        serverContainer.addEndpoint(new HelloServerConfig());

        server.start();
        server.join();
    }
}