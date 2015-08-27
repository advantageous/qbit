package io.advantageous.qbit.spring.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration holder for qbit application properties.
 *
 * @author geoffc@gmail.com (Geoff Chandler)
 */
@ConfigurationProperties("qbit.app")
public class AppProperties {

    private String prefix;
    private Integer healthCheckTtlSeconds = 5;
    private Integer statsFlushSeconds = 5;
    private Integer sampleEvery = 1000;
    private int adminPort;
    private String adminHost;
    private int jvmStatsRefresh;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public Integer getHealthCheckTtlSeconds() {
        return healthCheckTtlSeconds;
    }

    public void setHealthCheckTtlSeconds(Integer healthCheckTtlSeconds) {
        this.healthCheckTtlSeconds = healthCheckTtlSeconds;
    }

    public int getAdminPort() {
        return adminPort;
    }

    public void setAdminPort(int adminPort) {
        this.adminPort = adminPort;
    }

    public String getAdminHost() {
        return adminHost;
    }

    public void setAdminHost(String adminHost) {
        this.adminHost = adminHost;
    }

    public int getJvmStatsRefresh() {
        return jvmStatsRefresh;
    }

    public void setJvmStatsRefresh(int jvmStatsRefresh) {
        this.jvmStatsRefresh = jvmStatsRefresh;
    }

    public Integer getStatsFlushSeconds() {
        return statsFlushSeconds;
    }

    public void setStatsFlushSeconds(Integer statsFlushSeconds) {
        this.statsFlushSeconds = statsFlushSeconds;
    }

    public Integer getSampleEvery() {
        return sampleEvery;
    }

    public void setSampleEvery(Integer sampleEvery) {
        this.sampleEvery = sampleEvery;
    }
}
