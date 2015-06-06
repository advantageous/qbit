package io.advantageous.qbit.service.discovery.spi;

import io.advantageous.qbit.service.discovery.EndpointDefinition;
import io.advantageous.qbit.service.discovery.impl.ServiceHealthCheckIn;
import io.advantageous.qbit.util.ConcurrentHashSet;

import java.util.Collections;
import java.util.List;
import java.util.Queue;

/**
 * Service Discovery Provider.
 * created by rhightower on 3/24/15.
 */
public interface ServiceDiscoveryProvider {

    default void registerServices(Queue<EndpointDefinition> registerQueue) {
    }

    default void checkIn(Queue<ServiceHealthCheckIn> checkInsQueue) {
    }

    default List<EndpointDefinition> loadServices(String serviceName) {
        return Collections.emptyList();
    }

    default void unregisterServices(ConcurrentHashSet<EndpointDefinition> endpointDefinitions) {
    }
}
