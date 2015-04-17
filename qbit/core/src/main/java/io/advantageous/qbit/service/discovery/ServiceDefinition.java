package io.advantageous.qbit.service.discovery;

import io.advantageous.boon.core.Lists;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.UUID;

import static io.advantageous.qbit.service.discovery.ServiceDiscovery.uniqueString;

/**
 * Service Definition
 * Created by rhightower on 3/23/15.
 */
public class ServiceDefinition {

    private final HealthStatus healthStatus;
    private final String id;
    private final String name;
    private final String host;
    private final int port;
    private final long timeToLive;
    public ServiceDefinition(
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
        this.timeToLive = 20L;
    }
    public ServiceDefinition(
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

    public static List<ServiceDefinition> serviceDefinitions(final ServiceDefinition... serviceDefinitions) {
        return Lists.list(serviceDefinitions);
    }

    public static ServiceDefinition serviceDefinition(
            final String id,
            final String name,
            final String host,
            final int port) {

        return new ServiceDefinition(HealthStatus.PASS,
                id, name, host, port);
    }

    public static ServiceDefinition serviceDefinition(
            final String name,
            final String host,
            final int port) {

        return new ServiceDefinition(HealthStatus.PASS,
                name + "-" + uniqueString(port), name, host, port);
    }


    public static ServiceDefinition serviceDefinition(
            final String name,
            final String host
    ) {

        return new ServiceDefinition(HealthStatus.PASS,
                name + "-" + uniqueString(0), name, host, 0);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServiceDefinition)) return false;

        ServiceDefinition that = (ServiceDefinition) o;

        if (port != that.port) return false;
        if (healthStatus != that.healthStatus) return false;
        if (host != null ? !host.equals(that.host) : that.host != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
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
