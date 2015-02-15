package io.advantageous.qbit.http.jetty;

import io.advantageous.qbit.http.HttpRequest;
import io.advantageous.qbit.http.HttpServer;
import io.advantageous.qbit.http.WebSocketMessage;
import io.advantageous.qbit.http.config.HttpServerOptions;
import io.advantageous.qbit.http.jetty.impl.JettyQBitHttpServer;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.QueueBuilder;
import io.advantageous.qbit.spi.HttpServerFactory;
import io.advantageous.qbit.system.QBitSystemManager;

/**
 * Created by rhightower on 2/13/15.
 */
public class JettyHttpServerFactory implements HttpServerFactory {

    @Override
    public HttpServer create(HttpServerOptions options, QueueBuilder requestQueueBuilder,
                             QueueBuilder webSocketMessageQueueBuilder, QBitSystemManager systemManager) {
        return new JettyQBitHttpServer(options, systemManager);
    }
}
