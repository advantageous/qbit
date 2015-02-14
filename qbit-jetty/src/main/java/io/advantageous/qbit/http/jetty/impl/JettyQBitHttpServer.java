package io.advantageous.qbit.http.jetty.impl;

import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.http.HttpRequest;
import io.advantageous.qbit.http.impl.SimpleHttpServer;
import io.advantageous.qbit.system.QBitSystemManager;
import org.boon.Str;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetSocketAddress;

import static io.advantageous.qbit.servlet.QBitServletUtil.convertRequest;

/**
 * Created by rhightower on 2/13/15.
 */
public class JettyQBitHttpServer extends SimpleHttpServer {

    private final Logger logger = LoggerFactory.getLogger(SimpleHttpServer.class);
    private final boolean debug = false || GlobalConstants.DEBUG || logger.isDebugEnabled();
    private final Server server;
    private final QBitWebSocketHandler qBitWebSocketHandler ;
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

        final ThreadPool threadPool = this.server.getThreadPool();

        if (threadPool instanceof QueuedThreadPool) {
            if (httpWorkers > 4) {
                ((QueuedThreadPool) threadPool).setMaxThreads(httpWorkers);
                ((QueuedThreadPool) threadPool).setMinThreads(4);
            }
        }

        qBitWebSocketHandler = new QBitWebSocketHandler(this);
        server.setHandler(new AbstractHandler() {
            @Override
            public void handle(final String target,
                               final Request baseRequest,
                               final HttpServletRequest request,
                               final HttpServletResponse response)
                                    throws IOException, ServletException {
                baseRequest.setAsyncSupported(true);
                handleRequestInternal(request);
            }
        });
        //server.setHandler(qBitWebSocketHandler);


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
