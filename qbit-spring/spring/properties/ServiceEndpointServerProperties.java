package io.advantageous.qbit.spring.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration holder for ServiceEndpointServer properties.
 *
 * @author gcc@rd.io (Geoff Chandler)
 */
@ConfigurationProperties("qbit.service")
public class ServiceEndpointServerProperties {

    private String name;
    private String basePath;
    private int port;
    private int ttlSeconds;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getTtlSeconds() {
        return ttlSeconds;
    }

    public void setTtlSeconds(int ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }
}
