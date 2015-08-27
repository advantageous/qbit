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
    public ServiceEndpointServer serviceEndpointServer(final Queue<Response<Object>> queue,
                                                       final ServiceDiscovery serviceDiscovery,
                                                       final HealthServiceAsync healthServiceAsync,
                                                       final ServiceEndpointServerProperties props) {

        logger.info("Binding service {} with {} ttl to {}", props.getPort(), props.getTtlSeconds(), props.getPort());

        return EndpointServerBuilder.endpointServerBuilder()
                .setResponseQueue(queue)
                .setHealthService(healthServiceAsync)
                .setServiceDiscovery(serviceDiscovery)
                .setEndpointName(props.getName())
                .setUri(props.getBasePath())
                .setRequestBatchSize(100)
                .setFlushInterval(50)
                .setPort(props.getPort())
                .setTtlSeconds(props.getTtlSeconds())
                .build();
    }
}
