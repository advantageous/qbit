package io.advantageous.qbit.http.jetty;

import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.jetty.impl.JettyQBitHttpClient;
import io.advantageous.qbit.spi.HttpClientFactory;

/**
 * Created by rhightower on 2/13/15.
 */
public class JettyHttpClientFactory implements HttpClientFactory {

    @Override
    public HttpClient create(String host, int port, int requestBatchSize, int timeOutInMilliseconds,
                             int poolSize, boolean autoFlush, int flushRate,
                             boolean keepAlive, boolean pipeLine) {

        return new JettyQBitHttpClient(host, port);
    }
}
