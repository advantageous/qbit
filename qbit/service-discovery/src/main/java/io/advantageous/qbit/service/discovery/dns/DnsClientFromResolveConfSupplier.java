package io.advantageous.qbit.service.discovery.dns;

import io.vertx.core.Vertx;
import io.vertx.core.dns.DnsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;
import java.util.function.Supplier;

/**
 * If you don't supply a DNS port and host, then you get this class.
 * This class reads the information from /etc/resolv.conf.
 * If a host/port combo from /etc/resolv.conf fails, it uses the next one in the list.
 * If that one fails, it throws an exception.
 * Every try that fails will try the current DNS URI and the next one if the first fails.
 *
 */
public class DnsClientFromResolveConfSupplier implements Supplier<DnsClient>  {



    private final Logger logger = LoggerFactory.getLogger(DnsClientFromResolveConfSupplier.class);

    private final boolean debug = logger.isDebugEnabled();

    /**
     * Vertx instance. Vertx is used to build dns client.
     */
    private final Vertx vertx;


    /**
     * List of DNS addresses from /etc/resolv.conf.
     */
    private final List<URI> addressList;

    /**
     * Index of the current DNS address, this only increments if we have DNS failures.
     */
    private int index = 0;


    /**
     * Create DnsClientFromResolveConfSupplier
     * @param vertx vertx
     */
    public DnsClientFromResolveConfSupplier(final Vertx vertx) {

        this.vertx = vertx;
        addressList = DnsUtil.readDnsConf();

        if (debug) logger.debug("DnsClientFromResolveConfSupplier {}", addressList);
    }


    /**
     * Get a DNS client.
     * @return dns client.
     */
    @Override
    public DnsClient get() {

        final URI uri = addressList.get(index);

        try {

            if (debug) logger.debug("DnsClient.get port {} host {}", uri.getPort(), uri.getHost());
            return vertx.createDnsClient(uri.getPort(), uri.getHost());
        } catch (Exception ex) {


            logger.warn("DnsClient.get EXCEPTION port {} host {}", uri.getPort(), uri.getHost());
            if (index + 1 == addressList.size()) {
                index = 0;
            } else {
                index++;
            }
            final URI uri2 = addressList.get(index);

            if (debug) logger.debug("DnsClient.get FAIL OVER port {} host {}", uri2.getPort(), uri2.getHost());
            return vertx.createDnsClient(uri2.getPort(), uri2.getHost());
        }
    }
}
