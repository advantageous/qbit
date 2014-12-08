package io.advantageous.qbit.client;

import io.advantageous.qbit.http.HttpClient;
import io.advantageous.qbit.spi.ClientFactory;

/**
 * Created by rhightower on 12/3/14.
 */
public class BoonClientFactory implements ClientFactory {

    @Override
    public Client create(String uri, HttpClient httpClient) {
        return new BoonClient(uri, httpClient);
    }
}
