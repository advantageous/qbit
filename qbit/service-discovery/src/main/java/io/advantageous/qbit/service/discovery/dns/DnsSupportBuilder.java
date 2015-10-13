package io.advantageous.qbit.service.discovery.dns;

import io.vertx.core.Vertx;
import io.vertx.core.dns.DnsClient;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * DnsSupportBuilder knows how to build a DnsSupport class.
 */
public class DnsSupportBuilder {

    private final Vertx vertx;
    private int port = -1;
    private String host = null;
    private  Supplier<DnsClient> dnsClientProvider;
    private  Map<String, String> dnsServiceNameToServiceName;
    private  String postfixURL;

    public DnsSupportBuilder(Vertx vertx) {
        this.vertx = vertx;
    }


    public String getPostfixURL() {
        return postfixURL;
    }

    public DnsSupportBuilder setPostfixURL(String postfixURL) {
        this.postfixURL = postfixURL;
        return this;
    }

    public Vertx getVertx() {
        return vertx;
    }

    public int getPort() {
        return port;
    }

    public DnsSupportBuilder setPort(int port) {
        this.port = port;
        return this;
    }

    public String getHost() {
        return host;
    }

    public DnsSupportBuilder setHost(String host) {
        this.host = host;
        return this;
    }

    public Supplier<DnsClient> getDnsClientProvider() {
        if (dnsClientProvider == null) {

            if (port != -1) {
                dnsClientProvider = new DnsClientSupplier(getVertx(), getHost(), getPort());
            } else {
                dnsClientProvider = new DnsClientFromResolveConfSupplier(getVertx());
            }
        }
        return dnsClientProvider;
    }

    public DnsSupportBuilder setDnsClientProvider(Supplier<DnsClient> dnsClientProvider) {
        this.dnsClientProvider = dnsClientProvider;
        return this;
    }

    public Map<String, String> getDnsServiceNameToServiceName() {
        if (dnsServiceNameToServiceName == null) {
            dnsServiceNameToServiceName = new LinkedHashMap<>();
        }
        return dnsServiceNameToServiceName;
    }


    public DnsSupportBuilder addDnsServerToServiceNameMapping(
            final String dnsName, final String serviceName) {
        getDnsServiceNameToServiceName().put(dnsName, serviceName);
        return this;
    }


    public DnsSupportBuilder setDnsServiceNameToServiceName(Map<String, String> dnsServiceNameToServiceName) {
        this.dnsServiceNameToServiceName = dnsServiceNameToServiceName;
        return this;
    }

    public DnsSupport build() {
        return new DnsSupport(getDnsClientProvider(),
                getDnsServiceNameToServiceName(),
                getPostfixURL());
    }

    public static DnsSupportBuilder dnsSupportFactory(final Vertx vertx) {
        return new DnsSupportBuilder(vertx);
    }
}
