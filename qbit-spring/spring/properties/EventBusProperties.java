package io.advantageous.qbit.spring.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration holder for eventbus properties.
 *
 * @author gcc@rd.io (Geoff Chandler)
 */
@ConfigurationProperties("qbit.eventbus")
public class EventBusProperties {
    private String name;
    private Integer periodicCheckInSeconds;
    private Integer ttl;
    private int port;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPeriodicCheckInSeconds() {
        return periodicCheckInSeconds;
    }

    public void setPeriodicCheckInSeconds(Integer periodicCheckInSeconds) {
        this.periodicCheckInSeconds = periodicCheckInSeconds;
    }

    public Integer getTtl() {
        return ttl;
    }

    public void setTtl(Integer ttl) {
        this.ttl = ttl;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
