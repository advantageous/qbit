package io.advantageous.qbit.vertx.http;

import io.advantageous.qbit.http.server.HttpServer;
import io.advantageous.qbit.http.config.HttpServerOptions;
import io.advantageous.qbit.queue.QueueBuilder;
import io.advantageous.qbit.spi.HttpServerFactory;
import io.advantageous.qbit.system.QBitSystemManager;

/**
 * Created by rhightower on 1/26/15.
 */
public class HttpServerVertxFactory implements HttpServerFactory {


    @Override
    public HttpServer create(final HttpServerOptions options,
                             final QueueBuilder requestQueueBuilder,
                             final QueueBuilder responseQueueBuilder,
                             final QueueBuilder webSocketMessageQueueBuilder,
                             final QBitSystemManager systemManager) {

        if (options.isManageQueues() || requestQueueBuilder!=null
                || responseQueueBuilder!=null || webSocketMessageQueueBuilder!=null) {

            return new HttpServerVertxWithQueues(options, requestQueueBuilder,
                    responseQueueBuilder, webSocketMessageQueueBuilder, systemManager);

        } else {

            return new HttpServerVertx(options, systemManager);
        }
    }
}
