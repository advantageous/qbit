package io.advantageous.qbit.service.discovery.spi;

import io.advantageous.qbit.service.discovery.ServiceDefinition;
import io.advantageous.qbit.service.discovery.impl.ServiceHealthCheckIn;
import io.advantageous.qbit.util.ConcurrentHashSet;

import java.util.List;
import java.util.Queue;

/**
 * Service Discovery Provider.
 * Created by rhightower on 3/24/15.
 */
public interface ServiceDiscoveryProvider {

    void registerServices(Queue<ServiceDefinition> registerQueue);

    void checkIn(Queue<ServiceHealthCheckIn> checkInsQueue);

    List<ServiceDefinition> loadServices(String serviceName);

    default void unregisterServices(ConcurrentHashSet<ServiceDefinition> serviceDefinitions){}
}
