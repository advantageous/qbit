package io.advantageous.qbit.vertx.http;

import io.advantageous.qbit.http.HttpServer;
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
                             final QueueBuilder webSocketMessageQueueBuilder,
                             final QBitSystemManager systemManager) {

        return new HttpServerVertx(options, requestQueueBuilder, webSocketMessageQueueBuilder,
                systemManager);

    }
}
