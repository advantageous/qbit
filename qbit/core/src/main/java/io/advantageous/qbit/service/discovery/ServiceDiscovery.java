package io.advantageous.qbit.service.discovery;

import io.advantageous.qbit.service.Startable;
import io.advantageous.qbit.service.Stoppable;
import io.advantageous.qbit.service.health.HealthStatus;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Service Discovery
 * created by rhightower on 3/23/15.
 */
public interface ServiceDiscovery extends Startable, Stoppable {

    static String uniqueString(int port) {
        try {
            return port + "-" + InetAddress.getLocalHost().getHostName().replace('.', '-');
        } catch (UnknownHostException e) {
            return port + "-" + UUID.randomUUID().toString();
        }
    }

    default EndpointDefinition register(
            final String serviceName,
            final int port) {

        return new EndpointDefinition(HealthStatus.PASS,
                serviceName + "." + uniqueString(port),
                serviceName, null, port);
    }

    default EndpointDefinition registerWithTTL(
            final String serviceName,
            final int port,
            final int timeToLiveSeconds) {

        return new EndpointDefinition(HealthStatus.PASS,
                serviceName + "." + uniqueString(port),
                serviceName, null, port, timeToLiveSeconds);
    }

    default EndpointDefinition registerWithIdAndTimeToLive(
            final String serviceName, final String serviceId, final int port, final int timeToLiveSeconds) {

        return new EndpointDefinition(HealthStatus.PASS,
                serviceId,
                serviceName, null, port, timeToLiveSeconds);
    }

    default EndpointDefinition registerWithId(final String serviceName, final String serviceId, final int port) {

        return new EndpointDefinition(HealthStatus.PASS,
                serviceId,
                serviceName, null, port);
    }


    void watch(String serviceName);

    default void checkIn(String serviceId, HealthStatus healthStatus) {

    }


    default void checkInOk(String serviceId) {

    }

    default List<EndpointDefinition> loadServices(final String serviceName) {

        return Collections.emptyList();
    }

    default List<EndpointDefinition> loadServicesNow(final String serviceName) {

        return Collections.emptyList();
    }

    default void start() {
    }

    default void stop() {
    }


    default Set<EndpointDefinition> localDefinitions() {
        return Collections.emptySet();
    }
}
