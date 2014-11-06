package io.advantageous.qbit.vertx;

import io.advantageous.qbit.http.HttpServer;
import io.advantageous.qbit.spi.FactorySPI;
import io.advantageous.qbit.spi.HttpServerFactory;
import io.advantageous.qbit.vertx.http.HttpServerVertx;

/**
 * Created by rhightower on 11/6/14.
 */
public class RegisterVertxWithQBit {

    public static void registerVertxWithQBit() {
        FactorySPI.setHttpServerFactory(new HttpServerFactory() {
            @Override
            public HttpServer create(String host, int port) {
                return new HttpServerVertx(port, host);
            }
        });
    }
}
