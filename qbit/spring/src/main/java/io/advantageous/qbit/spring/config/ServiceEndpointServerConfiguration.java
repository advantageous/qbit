package io.advantageous.qbit.spring.config;

import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.server.EndpointServerBuilder;
import io.advantageous.qbit.server.ServiceEndpointServer;
import io.advantageous.qbit.service.discovery.ServiceDiscovery;
import io.advantageous.qbit.service.health.HealthServiceAsync;
import io.advantageous.qbit.spring.properties.ServiceEndpointServerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.inject.Named;
import java.util.Optional;

/**
 * Configuration for a service endpoint server.  This is enabled by the @EnableQBitServiceEndpointServer annotation.
 *
 * @author geoffc@gmail.com (Geoff Chandler)
 */
@Configuration
@EnableConfigurationProperties({ServiceEndpointServerProperties.class})
public class ServiceEndpointServerConfiguration {

    private final Logger logger = LoggerFactory.getLogger(ServiceEndpointServerConfiguration.class);

    @Bean
    public ServiceEndpointServer serviceEndpointServer(
            @Named("sharedResponseQueue") final Queue<Response<Object>> queue,
            final Optional<ServiceDiscovery> serviceDiscovery,
            final Optional<HealthServiceAsync> healthServiceAsync,
            final ServiceEndpointServerProperties props) {

        logger.info("Binding service {} with {} ttl to {}", props.getPort(), props.getTtlSeconds(), props.getPort());

        final EndpointServerBuilder builder = EndpointServerBuilder.endpointServerBuilder()
                .setResponseQueue(queue)
                .setEndpointName(props.getName())
                .setUri(props.getBasePath())
                .setFlushInterval(50)
                .setPort(props.getPort())
                .setTtlSeconds(props.getTtlSeconds());

        if (serviceDiscovery.isPresent()) builder.setServiceDiscovery(serviceDiscovery.get());
        if (healthServiceAsync.isPresent()) builder.setHealthService(healthServiceAsync.get());

        return builder.build();
    }
}
