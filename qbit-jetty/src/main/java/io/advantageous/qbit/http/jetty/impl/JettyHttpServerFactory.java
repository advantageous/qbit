package io.advantageous.qbit.http.jetty.impl;

import io.advantageous.qbit.http.HttpRequest;
import io.advantageous.qbit.http.HttpServer;
import io.advantageous.qbit.http.WebSocketMessage;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.spi.HttpServerFactory;
import io.advantageous.qbit.system.QBitSystemManager;

/**
 * Created by rhightower on 2/13/15.
 */
public class JettyHttpServerFactory implements HttpServerFactory {

    @Override
    public HttpServer create(String host, int port, boolean manageQueues, int pollTime,
                             int requestBatchSize, int flushInterval, int maxRequests,
                             QBitSystemManager systemManager) {
        return new JettyQBitHttpServer(host, port, flushInterval, -1, systemManager);
    }

    @Override
    public HttpServer createWithWorkers(String host, int port, boolean manageQueues,
                                        int pollTime, int requestBatchSize, int flushInterval,
                                        int maxRequests, int httpWorkers, Class handler,
                                        QBitSystemManager systemManager) {
        return new JettyQBitHttpServer(host, port, flushInterval, httpWorkers, systemManager);

    }

    @Override
    public HttpServer createHttpServerWithQueue(String host, int port, int flushInterval,
                                                Queue<HttpRequest> requestQueue,
                                                Queue<WebSocketMessage> webSocketMessageQueue,
                                                QBitSystemManager systemManager) {
        return new JettyQBitHttpServer(host, port, flushInterval, -1, systemManager);
    }
}
