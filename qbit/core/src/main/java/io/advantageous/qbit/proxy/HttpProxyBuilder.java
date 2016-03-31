package io.advantageous.qbit.proxy;

import io.advantageous.qbit.http.server.HttpServerBuilder;

public class HttpProxyBuilder {

    private HttpServerBuilder httpServerBuilder;

    private ProxyBuilder proxyBuilder;

    public static HttpProxyBuilder httpProxyBuilder() {
        return new HttpProxyBuilder();
    }

    public HttpServerBuilder getHttpServerBuilder() {
        if (httpServerBuilder == null) {
            httpServerBuilder = HttpServerBuilder.httpServerBuilder();
        }
        return httpServerBuilder;
    }

    public HttpProxyBuilder setHttpServerBuilder(HttpServerBuilder httpServerBuilder) {
        this.httpServerBuilder = httpServerBuilder;
        return this;
    }

    public ProxyBuilder getProxyBuilder() {
        if (proxyBuilder == null) {
            proxyBuilder = ProxyBuilder.proxyBuilder();
        }
        return proxyBuilder;
    }

    public HttpProxyBuilder setProxyBuilder(ProxyBuilder proxyBuilder) {
        this.proxyBuilder = proxyBuilder;
        return this;
    }

    public HttpProxy build() {
        return new HttpProxy(getHttpServerBuilder().build(), getProxyBuilder().buildProxy());
    }
}
