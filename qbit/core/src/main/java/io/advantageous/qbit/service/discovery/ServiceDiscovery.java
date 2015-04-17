package io.advantageous.qbit.service.discovery;

import io.advantageous.qbit.service.Startable;
import io.advantageous.qbit.service.Stoppable;
import io.advantageous.qbit.util.ConcurrentHashSet;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Service Discovery
 * Created by rhightower on 3/23/15.
 */
public interface ServiceDiscovery extends Startable, Stoppable {

    default ServiceDefinition register(
            final String serviceName,
            final int port) {

        return new ServiceDefinition(HealthStatus.PASS,
                serviceName + "." + uniqueString(port),
                serviceName, null, port);
    }



    default ServiceDefinition registerWithTTL(
            final String serviceName,
            final int port,
            final int timeToLiveSeconds) {

        return new ServiceDefinition(HealthStatus.PASS,
                serviceName + "." + uniqueString(port),
                serviceName, null, port, timeToLiveSeconds);
    }

    static String uniqueString(int port) {
        try {
            return port + "-" + InetAddress.getLocalHost().getHostName().replace('.', '-');
        } catch (UnknownHostException e) {
            return port + "-" + UUID.randomUUID().toString();
        }
    }


    default ServiceDefinition registerWithIdAndTimeToLive(
            final String serviceName, final String serviceId, final int port, final int timeToLiveSeconds) {

        return new ServiceDefinition(HealthStatus.PASS,
                serviceId,
                serviceName, null, port, timeToLiveSeconds);
    }

    default ServiceDefinition registerWithId(final String serviceName, final String serviceId, final int port) {

        return new ServiceDefinition(HealthStatus.PASS,
                serviceId,
                serviceName, null, port);
    }



    void watch(String serviceName);

    default void checkIn(String serviceId, HealthStatus healthStatus) {

    }


    default void checkInOk(String serviceId) {

    }

    default List<ServiceDefinition> loadServices(final String serviceName) {

        return Collections.emptyList();
    }

    default List<ServiceDefinition> loadServicesNow(final String serviceName) {

        return Collections.emptyList();
    }

    default void start() {}
    default void stop() {}


    default Set<ServiceDefinition> localDefinitions() {
       return Collections.emptySet();
    }
}
