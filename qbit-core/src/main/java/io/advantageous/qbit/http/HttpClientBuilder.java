package io.advantageous.qbit.http;

import io.advantageous.qbit.QBit;


/**
 * This allows one to construct an http client which attaches to a remote server.
 * @author rhightower
 * Created by rhightower on 11/13/14.
 */
public class HttpClientBuilder {


    private String host = "localhost";
    private int port = 8080;
    private int poolSize = 20;
    private int pollTime = 10;
    private int requestBatchSize = 10;
    private int timeOutInMilliseconds=3000;
    private boolean autoFlush = true;
    private boolean keepAlive = true;
    private boolean pipeline = true;


    public static HttpClientBuilder httpClientBuilder () {

        return new HttpClientBuilder();
    }
    public boolean isKeepAlive() {
        return keepAlive;
    }

    public HttpClientBuilder setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
        return this;
    }

    public boolean isPipeline() {
        return pipeline;
    }

    public HttpClientBuilder setPipeline(boolean pipeline) {
        this.pipeline = pipeline;
        return this;
    }

    public String getHost() {
        return host;
    }

    public HttpClientBuilder setHost(String host) {
        this.host = host;
        return this;
    }

    public int getPort() {
        return port;
    }

    public HttpClientBuilder setPort(int port) {
        this.port = port;
        return this;
    }


    public int getPollTime() {
        return pollTime;
    }

    public HttpClientBuilder setPollTime(int pollTime) {
        this.pollTime = pollTime;
        return this;
    }

    public int getRequestBatchSize() {
        return requestBatchSize;
    }

    public HttpClientBuilder setRequestBatchSize(int requestBatchSize) {
        this.requestBatchSize = requestBatchSize;
        return this;
    }



    public int getPoolSize() {
        return poolSize;
    }

    public HttpClientBuilder setPoolSize(int poolSize) {
        this.poolSize = poolSize;
        return this;
    }

    public int getTimeOutInMilliseconds() {
        return timeOutInMilliseconds;
    }

    public HttpClientBuilder setTimeOutInMilliseconds(int timeOutInMilliseconds) {
        this.timeOutInMilliseconds = timeOutInMilliseconds;
        return this;
    }

    public boolean isAutoFlush() {
        return autoFlush;
    }

    public HttpClientBuilder setAutoFlush(boolean autoFlush) {
        this.autoFlush = autoFlush;
        return this;
    }

    public HttpClient build() {
        final HttpClient httpClient = QBit.factory().createHttpClient(host, port, pollTime, requestBatchSize, timeOutInMilliseconds, poolSize, autoFlush, keepAlive, pipeline);

        return httpClient;
    }
}
