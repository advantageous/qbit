package io.advantageous.qbit.proxy;

import io.advantageous.qbit.http.request.HttpRequest;

/**
 * Created by rick on 10/27/15.
 */
public interface ProxyService {
    void handleRequest(HttpRequest request);
}
