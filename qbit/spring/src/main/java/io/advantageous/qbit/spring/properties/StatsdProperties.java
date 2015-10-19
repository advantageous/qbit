package io.advantageous.qbit.spring.properties;


import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration holder for statsd properties.
 *
 * @author geoffc@gmail.com (Geoff Chandler)
 */
@ConfigurationProperties("statsd")
public class StatsdProperties {

    private String host;
    private int port = 8125;
    private int bufferSize = 1500;
    private int flushRateIntervalMS = 2000;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public int getFlushRateIntervalMS() {
        return flushRateIntervalMS;
    }

    public void setFlushRateIntervalMS(int flushRateIntervalMS) {
        this.flushRateIntervalMS = flushRateIntervalMS;
    }
}
