package io.advantageous.qbit.proxy;

import io.advantageous.qbit.http.server.HttpServer;
import io.advantageous.qbit.service.Startable;
import io.advantageous.qbit.service.Stoppable;

import static io.advantageous.qbit.service.ServiceProxyUtils.flushServiceProxy;


/**
 * Marries a ProxyService to an HTTP endpoint and is a good example of how to use a ProxyService.
 */
public class HttpProxy implements Startable, Stoppable {


    /**
     * Http Server we are using this to listen to requests.
     */
    private final HttpServer server;

    /**
     * Proxy service used to forward requests to a backend.
     */
    private final ProxyService proxyService;

    /**
     * Constructor a new HttpProxy.
     **/
    public HttpProxy(final HttpServer server, ProxyService proxyService) {
        this.server = server;
        this.proxyService = proxyService;
    }

    /**
     * Start this.
     */
    public void start() {
        server.setHttpRequestConsumer(proxyService::handleRequest);
        server.setHttpRequestsIdleConsumer(aVoid -> flushServiceProxy(proxyService));
        server.startServer();
    }

    /**
     * Stop this.
     */
    @Override
    public void stop() {
        server.stop();
    }
}
