package io.advantageous.qbit.spi;

import io.advantageous.qbit.http.server.HttpServer;
import io.advantageous.qbit.http.config.HttpServerOptions;
import io.advantageous.qbit.queue.QueueBuilder;
import io.advantageous.qbit.system.QBitSystemManager;


public interface HttpServerFactory {

    HttpServer create(
            HttpServerOptions options,
            QueueBuilder requestQueueBuilder,
            QueueBuilder responseBuilder,
            QueueBuilder webSocketMessageQueueBuilder,
            QBitSystemManager systemManager
         );
}
