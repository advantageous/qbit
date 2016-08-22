package io.advantageous.qbit.service.discovery.lokate;

import io.advantageous.discovery.DiscoveryService;
import io.advantageous.qbit.service.discovery.EndpointDefinition;
import io.advantageous.qbit.service.discovery.ServiceDiscovery;
import io.advantageous.qbit.service.discovery.ServiceDiscoveryBuilder;
import io.advantageous.qbit.service.discovery.dns.DnsClientFromResolveConfSupplier;
import io.advantageous.qbit.service.discovery.dns.DnsServiceDiscoveryProviderBuilder;
import io.advantageous.qbit.service.discovery.dns.DnsSupportBuilder;
import io.advantageous.qbit.service.discovery.spi.ServiceDiscoveryProvider;
import io.advantageous.qbit.service.health.HealthStatus;
import io.vertx.core.Vertx;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import static io.advantageous.qbit.service.discovery.ServiceDiscoveryBuilder.*;

public class LokateServiceDiscoveryProvider implements ServiceDiscoveryProvider {

    private final DiscoveryService discoveryService;

    private LokateServiceDiscoveryProvider(final DiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    @Override
    public List<EndpointDefinition> loadServices(final String serviceName) {
        final URI serviceLookupURI = URI.create(serviceName);

        final List<URI> uris = discoveryService.lookupService(serviceName)
                .invokeAsBlockingPromise(Duration.ofSeconds(30)).get();

        return uris.stream().map(serviceResultURI -> {
            final String serviceName1 = serviceLookupURI.getPath().replace("/", "");
            final String serviceId = serviceName1 + "-" + serviceResultURI.getHost() + "-" + serviceResultURI.getPort();

            return new EndpointDefinition(HealthStatus.PASS, serviceId, serviceName1, serviceResultURI.getHost(),
                    serviceResultURI.getPort(), -1L);
        }).collect(Collectors.toList());

    }


    /**
     * Create service discovery that can talk via Lokate.
     * @param configs configs
     * @return ServiceDiscovery
     */
    public static ServiceDiscovery createLokateServiceDiscovery(final List<URI> configs) {

        return serviceDiscoveryBuilder()
                .setServiceDiscoveryProvider(
                        new LokateServiceDiscoveryProvider(
                                DiscoveryService.create(configs)))
                .build();

    }


    /**
     * Create service discovery that can talk via Lokate.
     * @param configs configs
     * @return ServiceDiscovery
     */
    public static ServiceDiscovery createLokateServiceDiscovery(final URI... configs) {

        return serviceDiscoveryBuilder()
                .setServiceDiscoveryProvider(
                        new LokateServiceDiscoveryProvider(
                                DiscoveryService.create(configs)))
                .build();

    }


}
