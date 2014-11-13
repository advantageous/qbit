package io.advantageous.qbit.vertx;

import io.advantageous.qbit.http.HttpClient;
import io.advantageous.qbit.http.HttpServer;
import io.advantageous.qbit.spi.FactorySPI;
import io.advantageous.qbit.spi.HttpClientFactory;
import io.advantageous.qbit.spi.HttpServerFactory;
import io.advantageous.qbit.vertx.http.HttpClientVertx;
import io.advantageous.qbit.vertx.http.HttpServerVertx;

/**
 * Created by rhightower on 11/6/14.
 */
public class RegisterVertxWithQBit {

    public static void registerVertxWithQBit() {
        FactorySPI.setHttpServerFactory(new HttpServerFactory() {

            @Override
            public HttpServer create(String host, int port, boolean manageQueues, int pollTime, int requestBatchSize, int flushInterval) {
                return new HttpServerVertx (port, host, manageQueues, pollTime, requestBatchSize, flushInterval);
            }
        });

        FactorySPI.setHttpClientFactory(new HttpClientFactory() {
            @Override
            public HttpClient create(String host, int port, int pollTime, int requestBatchSize, int timeOutInMilliseconds, int poolSize, boolean autoFlush) {
                return new HttpClientVertx(host, port, pollTime, requestBatchSize, timeOutInMilliseconds, poolSize, autoFlush);
            }
        });
    }
}
