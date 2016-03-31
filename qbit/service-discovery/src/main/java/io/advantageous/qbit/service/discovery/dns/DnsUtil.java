package io.advantageous.qbit.service.discovery.dns;

import io.advantageous.boon.core.IO;
import io.advantageous.boon.core.Str;
import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.service.discovery.ServiceDiscovery;
import io.advantageous.qbit.service.discovery.ServiceDiscoveryBuilder;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is a utility class that reads DNS hosts from /etc/resolv.conf.
 */
public class DnsUtil {

    public static final String QBIT_DNS_RESOLV_CONF = "QBIT_DNS_RESOLV_CONF";

    public static List<URI> readDnsConf() {

        final Logger logger = LoggerFactory.getLogger(DnsUtil.class);

        final boolean debug = logger.isDebugEnabled();

        final File file = new File(Sys.sysProp(QBIT_DNS_RESOLV_CONF, "/etc/resolv.conf"));


        if (file.exists()) {
            final List<String> lines = IO.readLines(file);

            if (debug) logger.debug("file contents {}", lines);

            return lines.stream().filter(line -> line.startsWith("nameserver"))
                    .map(line ->
                    {

                        if (debug) logger.debug("file content line = {}", line);
                        final String uriToParse = line.replace("nameserver ", "").trim();
                        final String[] split = Str.split(uriToParse, ':');
                        try {

                            if (split.length == 1) {
                                return new URI("dns", "", split[0], 53, "", "", "");
                            } else if (split.length >= 2) {
                                return new URI("dns", "", split[0], Integer.parseInt(split[1]), "", "", "");
                            } else {
                                throw new IllegalStateException("Unable to parse URI from /etc/resolv.conf");
                            }
                        } catch (URISyntaxException e) {
                            throw new IllegalStateException("failed to convert to URI");
                        }

                    })
                    .collect(Collectors.toList());
        } else {
            throw new IllegalStateException("" + file + " not found");
        }

    }

    /**
     * Create service discovery that can talk DNS.
     */
    public static ServiceDiscovery createDnsServiceDiscovery() {


        final ServiceDiscoveryBuilder serviceDiscoveryBuilder = ServiceDiscoveryBuilder.serviceDiscoveryBuilder();

        final Vertx vertx = Vertx.vertx();
        final DnsSupportBuilder dnsSupportBuilder = DnsSupportBuilder.dnsSupportBuilder(vertx)
                .setDnsClientSupplier(new DnsClientFromResolveConfSupplier(vertx));

        final DnsServiceDiscoveryProviderBuilder dnsServiceDiscoveryProviderBuilder =
                DnsServiceDiscoveryProviderBuilder.dnsServiceDiscoveryProviderBuilder()
                        .setDnsSupport(dnsSupportBuilder.build());


        return serviceDiscoveryBuilder
                .setServiceDiscoveryProvider(dnsServiceDiscoveryProviderBuilder.build()).build();
    }

}
