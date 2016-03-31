package io.advantageous.qbit.service.discovery.dns;

import io.vertx.core.Vertx;
import io.vertx.core.dns.DnsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provider abstracts how the DNS Client is created so we can unit test it.
 */
public class DnsSingleClientSupplier implements DnsClientSupplier {


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


    private final Logger logger = LoggerFactory.getLogger(DnsSingleClientSupplier.class);

    private final boolean debug = logger.isDebugEnabled();

    /**
     * DnsSingleClientSupplier constructor.
     *
     * @param vertx vertx
     * @param host  host
     * @param port  port
     */
    public DnsSingleClientSupplier(final Vertx vertx, final String host, final int port) {


        this.vertx = vertx;
        this.port = port;
        this.host = host;

        if (debug) logger.debug("DnsSingleClientSupplier( host = {}, port = {})", host, port);
    }

    /**
     * Supply an instance of DnsClient.
     *
     * @return DnsClient.
     */
    @Override
    public DnsClient get() {

        if (debug) logger.debug("DnsSingleClientSupplier.get()::host = {}, port = {}", host, port);
        return vertx.createDnsClient(port, host);
    }
}
