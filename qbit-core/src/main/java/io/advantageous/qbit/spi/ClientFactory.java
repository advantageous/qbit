package io.advantageous.qbit.spi;

import io.advantageous.qbit.client.Client;
import io.advantageous.qbit.http.HttpClient;

/**
 * Created by rhightower on 12/3/14.
 */
public interface ClientFactory {

    Client create(String uri, HttpClient httpClient) ;
}
