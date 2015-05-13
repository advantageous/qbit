package io.advantageous.qbit.service.discovery.spi;

import io.advantageous.qbit.service.discovery.EndpointDefinition;
import io.advantageous.qbit.service.discovery.impl.ServiceHealthCheckIn;
import io.advantageous.qbit.util.ConcurrentHashSet;

import java.util.List;
import java.util.Queue;

/**
 * Service Discovery Provider.
 * Created by rhightower on 3/24/15.
 */
public interface ServiceDiscoveryProvider {

    void registerServices(Queue<EndpointDefinition> registerQueue);

    void checkIn(Queue<ServiceHealthCheckIn> checkInsQueue);

    List<EndpointDefinition> loadServices(String serviceName);

    default void unregisterServices(ConcurrentHashSet<EndpointDefinition> endpointDefinitions){}
}
