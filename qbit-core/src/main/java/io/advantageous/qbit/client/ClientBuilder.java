package io.advantageous.qbit.client;

import io.advantageous.qbit.QBit;
import io.advantageous.qbit.http.HttpClient;

/**
 * Client builder is used to create a client programatically.
 *
 *
 *
 * Created by rhightower on 12/3/14.
 */
public class ClientBuilder {


    private String host = "localhost";
    private int port = 8080;
    private boolean autoFlush = true;
    private int pollTime = 100;
    private int poolSize = 10;

    private int requestBatchSize = 10;


    private int protocolBatchSize = -1;

    private int flushInterval = 100;
    private String uri = "/services";


    public String getUri() {
        return uri;
    }

    public ClientBuilder setUri(String uri) {
        this.uri = uri;
        return this;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public ClientBuilder setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
        return this;
    }

    private int timeoutSeconds = 30;

    public String getHost() {
        return host;
    }

    public ClientBuilder setHost(String host) {
        this.host = host;
        return this;
    }

    public int getPort() {
        return port;
    }

    public ClientBuilder setPort(int port) {
        this.port = port;
        return this;
    }

    public boolean isAutoFlush() {
        return autoFlush;
    }

    public ClientBuilder setAutoFlush(boolean autoFlush) {
        this.autoFlush = autoFlush;
        return this;
    }

    public int getPollTime() {
        return pollTime;
    }

    public ClientBuilder setPollTime(int pollTime) {
        this.pollTime = pollTime;
        return this;
    }

    public int getRequestBatchSize() {
        return requestBatchSize;
    }

    public ClientBuilder setRequestBatchSize(int requestBatchSize) {
        this.requestBatchSize = requestBatchSize;
        return this;
    }

    public int getFlushInterval() {
        return flushInterval;
    }

    public ClientBuilder setFlushInterval(int flushInterval) {
        this.flushInterval = flushInterval;
        return this;
    }


    public int getProtocolBatchSize() {
        return protocolBatchSize;
    }

    public ClientBuilder setProtocolBatchSize(int protocolBatchSize) {
        this.protocolBatchSize = protocolBatchSize;
        return this;
    }

    public Client build() {

        /**
         * String host, int port, int pollTime, int requestBatchSize, int timeOutInMilliseconds, int poolSize, boolean autoFlush
         */
        final HttpClient httpClient = QBit.factory().createHttpClient(host, port, pollTime, requestBatchSize, timeoutSeconds * 1000, poolSize, autoFlush);

        if (protocolBatchSize==-1) {
            protocolBatchSize = requestBatchSize;
        }

        Client client = QBit.factory().createClient(uri, httpClient, protocolBatchSize);
        return client;

    }

}
