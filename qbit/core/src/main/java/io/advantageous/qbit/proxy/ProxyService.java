package io.advantageous.qbit.proxy;

import io.advantageous.qbit.http.request.HttpRequest;

/**
 * ProxyService interface to proxy services to the backend.
 */
public interface ProxyService {
    void handleRequest(HttpRequest request);
}
