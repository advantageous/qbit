package io.advantageous.qbit.vertx.service;

import io.advantageous.qbit.client.Client;
import io.advantageous.qbit.http.HttpClient;
import io.advantageous.qbit.spi.ClientFactory;

/**
 * Created by rhightower on 12/3/14.
 */
public class VertxClientFactory implements ClientFactory {

    @Override
    public Client create(String uri, HttpClient httpClient) {
        return new VertxClient(uri, httpClient);
    }
}
