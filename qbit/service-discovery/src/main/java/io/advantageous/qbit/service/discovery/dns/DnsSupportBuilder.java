package io.advantageous.qbit.service.discovery.dns;

import io.vertx.core.Vertx;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * DnsSupportBuilder knows how to build a DnsSupport class.
 */
public class DnsSupportBuilder {

    private final Vertx vertx;
    private int port = -1;
    private String host = null;
    private DnsClientSupplier dnsClientSupplier;
    private Map<String, String> dnsServiceNameToServiceName;
    private String postfixURL;

    public DnsSupportBuilder(Vertx vertx) {
        this.vertx = vertx;
    }

    @Deprecated
    public static DnsSupportBuilder dnsSupportFactory(final Vertx vertx) {
        return new DnsSupportBuilder(vertx);
    }

    public static DnsSupportBuilder dnsSupportBuilder(final Vertx vertx) {
        return new DnsSupportBuilder(vertx);
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

    public DnsClientSupplier getDnsClientSupplier() {
        if (dnsClientSupplier == null) {

            if (port != -1) {
                dnsClientSupplier = new DnsSingleClientSupplier(getVertx(), getHost(), getPort());
            } else {
                dnsClientSupplier = new DnsClientFromResolveConfSupplier(getVertx());
            }
        }
        return dnsClientSupplier;
    }

    public DnsSupportBuilder setDnsClientSupplier(DnsClientSupplier dnsClientSupplier) {
        this.dnsClientSupplier = dnsClientSupplier;
        return this;
    }

    public Map<String, String> getDnsServiceNameToServiceName() {
        if (dnsServiceNameToServiceName == null) {
            dnsServiceNameToServiceName = new LinkedHashMap<>();
        }
        return dnsServiceNameToServiceName;
    }

    public DnsSupportBuilder setDnsServiceNameToServiceName(Map<String, String> dnsServiceNameToServiceName) {
        this.dnsServiceNameToServiceName = dnsServiceNameToServiceName;
        return this;
    }

    public DnsSupportBuilder addDnsServerToServiceNameMapping(
            final String dnsName, final String serviceName) {
        getDnsServiceNameToServiceName().put(dnsName, serviceName);
        return this;
    }

    public DnsSupport build() {
        return new DnsSupport(getDnsClientSupplier(),
                getDnsServiceNameToServiceName(),
                getPostfixURL());
    }
}
