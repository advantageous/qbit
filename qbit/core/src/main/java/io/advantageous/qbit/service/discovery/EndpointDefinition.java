package io.advantageous.qbit.service.discovery;

import io.advantageous.boon.core.Lists;
import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.service.health.HealthStatus;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import static io.advantageous.qbit.service.discovery.ServiceDiscovery.uniqueString;

/**
 * Service Definition
 * Contains a healthStatus, unique id, name, host, port and a timeToLive in seconds.
 * This describes all parts of a service as far as something like a ServiceDiscovery system like
 * [Consul](https://consul.io/) is concerned.
 * <p>
 * The `timeToLive` field is for ttl checkins if the underlying system supports it.
 * <p>
 * The `HealthStatus` represents the current state of this system as returned from the remote
 * service discovery system.
 * <p>
 * created by rhightower on 3/23/15.
 */
public class EndpointDefinition {


    /**
     * Current health status.
     */
    private final HealthStatus healthStatus;

    /**
     * Unique id of the system.
     */
    private final String id;

    /**
     * Name of the service, i.e., EventBus, StatsEngine, etc.
     */
    private final String name;

    /**
     * Host name.
     */
    private final String host;

    /**
     * Port of the service.
     */
    private final int port;

    /**
     * Time to live: how long until this service has to check in with the remote service discovery
     * system if applicable. Whether this is used or needed depends on the underlying service discovery system.
     */
    private final long timeToLive;


    /**
     * Create a new one with default TTL of 20 seconds.
     *
     * @param healthStatus healthStatus
     * @param id           id
     * @param name         name
     * @param host         post
     * @param port         port
     */
    public EndpointDefinition(
            final HealthStatus healthStatus,
            final String id,
            final String name,
            final String host,
            final int port) {
        this.healthStatus = healthStatus;
        this.id = id;
        this.name = name;
        this.host = host;
        this.port = port;
        this.timeToLive = Sys.sysProp(EndpointDefinition.class.getName() + ".timeToLive", 20L);
    }


    /**
     * Create a new one with default TTL of 20 seconds.
     *
     * @param name name
     * @param host post
     * @param port port
     */
    public EndpointDefinition(
            final String name,
            final String host,
            final int port) {
        this.healthStatus = HealthStatus.PASS;
        this.id = name + "-" + port + "-" + host.replace('.', '-');
        this.name = name;
        this.host = host;
        this.port = port;
        this.timeToLive = Sys.sysProp(EndpointDefinition.class.getName() + ".timeToLive", 20L);
    }

    /**
     * Create a new one with default TTL of 20 seconds.
     *
     * @param healthStatus healthStatus
     * @param id           id
     * @param name         name
     * @param host         post
     * @param port         port
     */
    public EndpointDefinition(
            final HealthStatus healthStatus,
            final String id,
            final String name,
            final String host,
            final int port,
            final long timeToLive) {
        this.healthStatus = healthStatus;
        this.id = id;
        this.name = name;
        this.host = host;
        this.port = port;
        this.timeToLive = timeToLive;
    }

    /**
     * Find host
     *
     * @return hostname
     */
    static String findHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new IllegalStateException("unable to find host name");
        }
    }

    /**
     * Creates a list of service definitions.
     *
     * @param endpointDefinitions vararg array of service definitions
     * @return list of service definitions
     */
    public static List<EndpointDefinition> serviceDefinitions(final EndpointDefinition... endpointDefinitions) {
        return Lists.list(endpointDefinitions);
    }

    /**
     * Creates a EndpointDefinition for a service, i.e., a serviceDefinition.
     *
     * @param name name
     * @return serviceDefinition
     */
    public static EndpointDefinition serviceDefinition(final String name) {

        return new EndpointDefinition(HealthStatus.PASS,
                name + "-" + uniqueString(0), name, findHostName(), 0);
    }

    /**
     * Creates a EndpointDefinition for a service, i.e., a serviceDefinition.
     *
     * @param name service name
     * @param port port
     * @return serviceDefinition
     */
    public static EndpointDefinition serviceDefinition(final String name, int port) {

        return new EndpointDefinition(HealthStatus.PASS,
                name + "-" + uniqueString(port), name, findHostName(), 0);
    }

    /**
     * Creates a EndpointDefinition for a service, i.e., a serviceDefinition.
     *
     * @param id   id
     * @param name name
     * @param host host
     * @param port port
     * @return EndpointDefinition
     */
    public static EndpointDefinition serviceDefinition(
            final String id,
            final String name,
            final String host,
            final int port) {

        return new EndpointDefinition(HealthStatus.PASS,
                id, name, host, port);
    }

    /**
     * Creates a EndpointDefinition for a service, i.e., a serviceDefinition.
     *
     * @param name name
     * @param host host
     * @param port port
     * @return serviceDefinition
     */
    public static EndpointDefinition serviceDefinition(
            final String name,
            final String host,
            final int port) {

        return new EndpointDefinition(HealthStatus.PASS,
                name + "-" + uniqueString(port), name, host, port);
    }


    /**
     * Creates a EndpointDefinition for a service, i.e., a serviceDefinition.
     *
     * @param name name
     * @param host host
     * @return serviceDefinition
     */
    public static EndpointDefinition serviceDefinition(
            final String name,
            final String host
    ) {

        return new EndpointDefinition(HealthStatus.PASS,
                name + "-" + uniqueString(0), name, host, 0);
    }


    /**
     * Creates a EndpointDefinition for a service, i.e., a serviceDefinition.
     *
     * @param id   id
     * @param name name
     * @param host host
     * @return EndpointDefinition
     */
    public static EndpointDefinition serviceDefinitionWithId(
            final String name,
            final String host,
            final String id) {

        return new EndpointDefinition(HealthStatus.PASS,
                id, name, host, 0);
    }

    public HealthStatus getHealthStatus() {
        return healthStatus;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EndpointDefinition)) return false;

        EndpointDefinition that = (EndpointDefinition) o;

        if (port != that.port) return false;
        if (healthStatus != that.healthStatus) return false;
        if (host != null ? !host.equals(that.host) : that.host != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        return !(name != null ? !name.equals(that.name) : that.name != null);

    }

    @Override
    public int hashCode() {
        int result = healthStatus != null ? healthStatus.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (host != null ? host.hashCode() : 0);
        result = 31 * result + port;
        return result;
    }

    @Override
    public String toString() {
        return "ServiceDefinition{" +
                "status=" + healthStatus +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                '}';
    }

    public long getTimeToLive() {

        return timeToLive;
    }
}
