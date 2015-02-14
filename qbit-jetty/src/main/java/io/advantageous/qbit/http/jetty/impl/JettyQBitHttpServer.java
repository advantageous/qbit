package io.advantageous.qbit.http.jetty.impl;

import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.http.HttpRequest;
import io.advantageous.qbit.http.HttpServer;
import io.advantageous.qbit.http.WebSocketMessage;
import io.advantageous.qbit.http.impl.SimpleHttpServer;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.system.QBitSystemManager;
import org.boon.Str;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.jsr356.server.ServerContainer;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import static io.advantageous.qbit.servlet.QBitServletUtil.convertRequest;

/**
 * Created by rhightower on 2/13/15.
 */
public class JettyQBitHttpServer extends SimpleHttpServer {


    private final Logger logger = LoggerFactory.getLogger(SimpleHttpServer.class);
    private final boolean debug = false || GlobalConstants.DEBUG || logger.isDebugEnabled();


    private final Server server;

    public JettyQBitHttpServer(final String host,
                               final int port,
                               final int flushInterval,
                               final int httpWorkers,
                               final QBitSystemManager systemManager) {
        super(systemManager, flushInterval);

        if (Str.isEmpty(host)) {
            this.server = new Server(port);
        } else {
            InetSocketAddress inetSocketAddress = new InetSocketAddress(host, port);
            this.server = new Server(inetSocketAddress);
        }

        server.setHandler(new AbstractHandler() {
            @Override
            public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {


                handleRequestInternal(request);
            }
        });




    }

    private void handleRequestInternal(final HttpServletRequest request) {
        final HttpRequest httpRequest = convertRequest(request.startAsync());
        super.handleRequest(httpRequest);
    }

    @Override
    public void start() {
        super.start();
        try {
            server.start();
        } catch (Exception ex) {
            logger.error("Unable to start up Jetty", ex);
        }
    }


    public void stop() {
        super.stop();
        try {
            server.stop();
        } catch (Exception ex) {
            logger.error("Unable to shut down Jetty", ex);
        }
    }
}
