package io.advantageous.qbit.spi;

import io.advantageous.qbit.http.HttpServer;


public interface HttpServerFactory {

    HttpServer create(String host, int port);


    HttpServer create(String host, int port, boolean manageQueues,
         int pollTime,
         int requestBatchSize,
         int flushInterval);



}
