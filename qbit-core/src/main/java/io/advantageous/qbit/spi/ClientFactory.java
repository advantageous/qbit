package io.advantageous.qbit.spi;

import io.advantageous.qbit.client.Client;
import io.advantageous.qbit.http.HttpClient;

/**
 * Creates a client.
 * This gets used by QBit factory to create a client.
 * Created by rhightower on 12/3/14.
 * @author rhightower
 */
public interface ClientFactory {

    Client create(String uri, HttpClient httpClient, int requestBatchSize) ;
}
