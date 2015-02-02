package io.advantageous.qbit.vertx.http;

import io.advantageous.qbit.http.HttpServer;
import io.advantageous.qbit.spi.HttpServerFactory;

/**
 * Created by rhightower on 1/26/15.
 */
public class HttpServerVertxFactory implements HttpServerFactory {
    @Override
    public HttpServer create(String host, int port, boolean manageQueues, int pollTime, int requestBatchSize, int flushInterval, int maxRequests) {
        return new HttpServerVertx(port, host, manageQueues, pollTime, requestBatchSize, flushInterval, maxRequests);
    }

    @Override
    public HttpServer create(String host, int port, boolean manageQueues, int pollTime, int requestBatchSize, int flushInterval, int maxRequests, int httpWorkers, Class handler) {
        return new HttpServerVertx(port, host, manageQueues, pollTime, requestBatchSize, flushInterval, maxRequests, httpWorkers, handler);

    }
}
