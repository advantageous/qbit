package io.advantageous.qbit.vertx.http.client;

import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.spi.HttpClientFactory;

import java.util.function.Consumer;

public class HttpVertxClientFactory implements HttpClientFactory {
    @Override
    public HttpClient create(String host,
                             int port,
                             int timeOutInMilliseconds,
                             int poolSize,
                             boolean autoFlush,
                             int flushRate,
                             boolean keepAlive,
                             boolean pipeLine,
                             boolean ssl,
                             boolean verifyHost,
                             boolean trustAll,
                             int maxWebSocketFrameSize,
                             boolean tryUseCompression,
                             String trustStorePath,
                             String trustStorePassword,
                             boolean tcpNoDelay,
                             int soLinger,
                             Consumer<Throwable> errorHandler) {
        return new HttpVertxClient(host, port, timeOutInMilliseconds, poolSize, autoFlush,
                flushRate, keepAlive, pipeLine, ssl, verifyHost, trustAll, maxWebSocketFrameSize,
                tryUseCompression, trustStorePath, trustStorePassword, tcpNoDelay, soLinger, errorHandler);
    }
}
