package io.advantageous.qbit.proxy;

import io.advantageous.qbit.http.server.HttpServer;


public class HttpProxy {


    private final HttpServer server;

    private final ProxyService proxyService;

    public HttpProxy(final HttpServer server, ProxyService proxyService) {
        this.server = server;
        this.proxyService = proxyService;
    }

    public void init() {
       server.setHttpRequestConsumer(httpRequest -> {
            proxyService.handleRequest(httpRequest);
       });

        server.startServer();
    }

}
