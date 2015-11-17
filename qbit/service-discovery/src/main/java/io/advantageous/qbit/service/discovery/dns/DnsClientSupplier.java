package io.advantageous.qbit.service.discovery.dns;

import io.vertx.core.Vertx;
import io.vertx.core.dns.DnsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * Provider abstracts how the DNS Client is created so we can unit test it.
 */
public class DnsClientSupplier implements Supplier<DnsClient> {


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


    private final Logger logger = LoggerFactory.getLogger(DnsClientSupplier.class);

    private final boolean debug = logger.isDebugEnabled();

    /**
     * DnsClientSupplier constructor.
     * @param vertx vertx
     * @param host host
     * @param port port
     */
    public DnsClientSupplier(final Vertx vertx, final String host, final int port) {



        this.vertx = vertx;
        this.port = port;
        this.host = host;

        if (debug) logger.debug("DnsClientSupplier( host = {}, port = {})", host, port);
    }

    /**
     * Supply an instance of DnsClient.
     * @return DnsClient.
     */
    @Override
    public DnsClient get() {

        if (debug) logger.debug("DnsClientSupplier.get()::host = {}, port = {}", host, port);
        return vertx.createDnsClient(port, host);
    }
}
