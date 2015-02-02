package io.advantageous.qbit.spi;

import io.advantageous.qbit.http.HttpServer;


public interface HttpServerFactory {

    HttpServer create(String host, int port, boolean manageQueues,
         int pollTime,
         int requestBatchSize,
         int flushInterval, int maxRequests
         );

    default HttpServer create(String host, int port, boolean manageQueues,
                      int pollTime,
                      int requestBatchSize,
                      int flushInterval, int maxRequests, int httpWorkers, Class handler
    ) {
        throw new RuntimeException("NOT IMPLEMENTED BY FACTORY " + this.getClass());
    }



}
