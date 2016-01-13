package io.advantageous.qbit.example.proxy;

import io.advantageous.qbit.http.server.HttpServerBuilder;
import io.advantageous.qbit.proxy.HttpProxy;
import io.advantageous.qbit.proxy.HttpProxyBuilder;
import io.advantageous.qbit.proxy.ProxyBuilder;

public class HttpProxyServerMain {

    public static void main(String... args) throws Exception {
        final HttpProxyBuilder httpProxyBuilder = HttpProxyBuilder.httpProxyBuilder();
        final HttpServerBuilder httpServerBuilder = httpProxyBuilder.getHttpServerBuilder();
        httpServerBuilder.setPort(9090);
        final ProxyBuilder proxyBuilder = httpProxyBuilder.getProxyBuilder();
        proxyBuilder.getHttpClientBuilder().setPort(8080);

        final HttpProxy httpProxy = httpProxyBuilder.build();

        httpProxy.start();
    }

}
