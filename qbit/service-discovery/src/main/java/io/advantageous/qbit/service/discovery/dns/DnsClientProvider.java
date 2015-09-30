package io.advantageous.qbit.service.discovery.dns;

import io.vertx.core.Vertx;
import io.vertx.core.dns.DnsClient;

import java.util.function.Supplier;

/**
 * Provider abstracts how the DNS Client is created so we can unit test it.
 */
public class DnsClientProvider implements Supplier<DnsClient> {


    /**
     * Vertx instance. Vertx is used to build dns client.
     */
    private final Vertx vertx;

    /**
     * port of DNS server.
     */
    private final int port;

    /**
     * Host of DNS server.
     */
    private final String host;

    /**
     * DnsClientProvider constructor.
     * @param vertx vertx
     * @param host host
     * @param port port
     */
    public DnsClientProvider(final Vertx vertx, final String host, final int port) {
        this.vertx = vertx;
        this.port = port;
        this.host = host;
    }

    /**
     * Supply an instance of DnsClient.
     * @return DnsClient.
     */
    @Override
    public DnsClient get() {
        return vertx.createDnsClient(port, host);
    }
}
