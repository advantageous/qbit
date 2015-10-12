package io.advantageous.qbit.service.discovery.dns;

import io.vertx.core.Vertx;
import io.vertx.core.dns.DnsClient;

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
    }


    /**
     * Get a DNS client.
     * @return dns client.
     */
    @Override
    public DnsClient get() {

        final URI uri = addressList.get(index);

        try {
            return vertx.createDnsClient(uri.getPort(), uri.getHost());
        } catch (Exception ex) {
            if (index + 1 == addressList.size()) {
                index = 0;
            } else {
                index++;
            }
            final URI uri2 = addressList.get(index);
            return vertx.createDnsClient(uri2.getPort(), uri2.getHost());
        }
    }
}
