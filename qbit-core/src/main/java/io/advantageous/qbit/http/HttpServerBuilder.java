package io.advantageous.qbit.http;

import io.advantageous.qbit.QBit;

import java.util.function.Consumer;

/**
 * Allows one to build an HTTP server.
 * @author rhightower
 * Created by Richard on 11/12/14.
 */
public class HttpServerBuilder {

    private String host;
    private int port = 8080;
    private boolean manageQueues = true;

    private boolean pipeline = true;
    private int pollTime = 100;
    private int requestBatchSize = 10;
    private int flushInterval = 100;

    private Consumer<WebSocketMessage> webSocketMessageConsumer;
    private Consumer<HttpRequest> httpRequestConsumer;

    public boolean isPipeline() {
        return pipeline;
    }

    public HttpServerBuilder setPipeline(boolean pipeline) {
        this.pipeline = pipeline;
        return this;
    }

    public String getHost() {
        return host;
    }

    public HttpServerBuilder setHost(String host) {
        this.host = host;
        return this;
    }

    public int getPort() {
        return port;
    }

    public HttpServerBuilder setPort(int port) {
        this.port = port;
        return this;
    }

    public boolean isManageQueues() {
        return manageQueues;
    }

    public HttpServerBuilder setManageQueues(boolean manageQueues) {
        this.manageQueues = manageQueues;
        return this;
    }

    public int getPollTime() {
        return pollTime;
    }

    public HttpServerBuilder setPollTime(int pollTime) {
        this.pollTime = pollTime;
        return this;
    }

    public int getRequestBatchSize() {
        return requestBatchSize;
    }

    public HttpServerBuilder setRequestBatchSize(int requestBatchSize) {
        this.requestBatchSize = requestBatchSize;
        return this;
    }

    public int getFlushInterval() {
        return flushInterval;
    }

    public HttpServerBuilder setFlushInterval(int flushInterval) {
        this.flushInterval = flushInterval;
        return this;
    }

    public Consumer<WebSocketMessage> getWebSocketMessageConsumer() {
        return webSocketMessageConsumer;
    }

    public HttpServerBuilder setWebSocketMessageConsumer(Consumer<WebSocketMessage> webSocketMessageConsumer) {
        this.webSocketMessageConsumer = webSocketMessageConsumer;
        return this;
    }

    public Consumer<HttpRequest> getHttpRequestConsumer() {
        return httpRequestConsumer;
    }

    public HttpServerBuilder setHttpRequestConsumer(Consumer<HttpRequest> httpRequestConsumer) {
        this.httpRequestConsumer = httpRequestConsumer;
        return this;
    }

    public HttpServer build() {
        final HttpServer httpServer = QBit.factory().createHttpServer(host, port, manageQueues, pollTime, requestBatchSize, flushInterval);
        httpServer.setHttpRequestConsumer(this.httpRequestConsumer);
        httpServer.setWebSocketMessageConsumer(this.webSocketMessageConsumer);
        return httpServer;
    }
}
