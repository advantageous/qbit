package io.advantageous.qbit.proxy;

import io.advantageous.qbit.http.server.HttpServer;
import io.advantageous.qbit.service.ServiceProxyUtils;
import io.advantageous.qbit.service.Startable;
import io.advantageous.qbit.service.Stoppable;


public class HttpProxy implements Startable, Stoppable{


    private final HttpServer server;

    private final ProxyService proxyService;

    public HttpProxy(final HttpServer server, ProxyService proxyService) {
        this.server = server;
        this.proxyService = proxyService;
    }

    public void start() {
       server.setHttpRequestConsumer(httpRequest -> {
            proxyService.handleRequest(httpRequest);
            ServiceProxyUtils.flushServiceProxy(proxyService);

       });

        server.startServer();
    }

    @Override
    public void stop() {
        server.stop();
    }
}
