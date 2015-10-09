package io.advantageous.qbit.spring.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration holder for ServiceEndpointServer properties.
 *
 * @author geoffc@gmail.com (Geoff Chandler)
 */
@ConfigurationProperties("qbit.service")
public class ServiceEndpointServerProperties {

    private String name = "service-endpoint";
    private String basePath = "/services";
    private int port = 8080;
    private int ttlSeconds = 10;

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
