package io.advantageous.qbit.spi;

import io.advantageous.qbit.http.HttpServer;
import io.advantageous.qbit.http.config.HttpServerOptions;
import io.advantageous.qbit.queue.QueueBuilder;
import io.advantageous.qbit.system.QBitSystemManager;


public interface HttpServerFactory {


    HttpServer create(
            HttpServerOptions options,
            QueueBuilder requestQueueBuilder,
            QueueBuilder webSocketMessageQueueBuilder,
            QBitSystemManager systemManager
         );

}
