package io.advantageous.qbit.proxy;

import io.advantageous.qbit.http.request.HttpRequest;

public interface ProxyService {
    void handleRequest(HttpRequest request);
}
