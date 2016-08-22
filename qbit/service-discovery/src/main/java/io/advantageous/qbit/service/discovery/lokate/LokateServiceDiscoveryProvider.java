package io.advantageous.qbit.service.discovery.lokate;

import io.advantageous.discovery.DiscoveryService;
import io.advantageous.qbit.service.discovery.EndpointDefinition;
import io.advantageous.qbit.service.discovery.ServiceDiscovery;
import io.advantageous.qbit.service.discovery.spi.ServiceDiscoveryProvider;
import io.advantageous.qbit.service.health.HealthStatus;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import static io.advantageous.qbit.service.discovery.ServiceDiscoveryBuilder.serviceDiscoveryBuilder;

public class LokateServiceDiscoveryProvider implements ServiceDiscoveryProvider {

    private final DiscoveryService discoveryService;

    private LokateServiceDiscoveryProvider(final DiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    /**
     * Create service discovery that can talk via Lokate.
     *
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
     *
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

    @Override
    public List<EndpointDefinition> loadServices(final String serviceName) {
        final URI serviceLookupURI = URI.create(URI.create(serviceName).getSchemeSpecificPart());

        final List<URI> uris = discoveryService.lookupService(serviceName)
                .invokeAsBlockingPromise(Duration.ofSeconds(30)).get();

        return uris.stream().map(serviceResultURI -> {
            final String serviceName1 = serviceLookupURI.getSchemeSpecificPart().replace("/", "")
                    .replace(":", "_").replace("?", "_").replace("=", "_").replace("&", "_");
            final String serviceId = serviceName1 + "-" + serviceResultURI.getHost() + "-" + serviceResultURI.getPort();

            return new EndpointDefinition(HealthStatus.PASS, serviceId, serviceName1, serviceResultURI.getHost(),
                    serviceResultURI.getPort(), -1L);
        }).collect(Collectors.toList());

    }


}
