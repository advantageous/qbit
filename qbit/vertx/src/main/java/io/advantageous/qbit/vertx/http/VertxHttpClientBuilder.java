package io.advantageous.qbit.vertx.http;

import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.client.HttpClientBuilder;
import io.advantageous.qbit.vertx.http.client.HttpVertxClient;
import io.vertx.core.Vertx;


/**
 * This allows one to construct an http client which attaches to a remote server.
 * It also allows one to pass a shared Vertx object if running inside of the Vertx world.
 * <p>
 * ## If you are working with Vertx direct
 * <p>
 * Then you use this class to marry the QBit and Vertx worlds.
 * <p>
 * ## Usage
 * <p>
 * ```java
 * vertxHttpClientBuilder = VertxHttpClientBuilder.vertxHttpClientBuilder().setVertx(vertx);
 * ...
 * <p>
 * HttpClient httpClient = vertxHttpClientBuilder.setPort(9090).setHost("localhost").build();
 * <p>
 * <p>
 * ```
 *
 * @author rhightower
 */
public class VertxHttpClientBuilder extends HttpClientBuilder {

    private Vertx vertx;

    public static VertxHttpClientBuilder vertxHttpClientBuilder() {
        return new VertxHttpClientBuilder();
    }

    public Vertx getVertx() {
        if (vertx == null) {
            vertx = Vertx.vertx();
        }
        return vertx;
    }


    public VertxHttpClientBuilder setVertx(final Vertx vertx) {
        this.vertx = vertx;
        return this;
    }


    public HttpClient build() {

        return new HttpVertxClient(super.getHost(), super.getPort(), super.getTimeOutInMilliseconds(),
                super.getPoolSize(), super.isAutoFlush(), super.getFlushInterval(), super.isKeepAlive(),
                super.isPipeline(), super.isSsl(), super.isVerifyHost(), super.isTrustAll(),
                super.getMaxWebSocketFrameSize(), super.isTryUseCompression(), super.getTrustStorePath(),
                super.getTrustStorePassword(),
                super.isTcpNoDelay(), super.getSoLinger(), super.getErrorHandler());
    }
}
