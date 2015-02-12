package io.advantageous.qbit.http;

import io.advantageous.qbit.QBit;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.QueueBuilder;
import io.advantageous.qbit.system.QBitSystemManager;

import java.util.function.Consumer;

/**
 * Allows one to build().start() an HTTP server.
 * @author rhightower
 * Created by Richard on 11/12/14.
 */
public class HttpServerBuilder {

    public static HttpServerBuilder httpServerBuilder() {
        return new HttpServerBuilder();
    }

    private String host;
    private int port = 8080;
    private boolean manageQueues = true;
    private int maxRequestBatches = 1_000_000;
    private boolean pipeline = true;
    private int pollTime = 100;
    private int requestBatchSize = 10;
    private int flushInterval = 100;
    private int workers = -1;
    private Class<Consumer> handlerClass = null;
    private Consumer<WebSocketMessage> webSocketMessageConsumer;
    private Consumer<HttpRequest> httpRequestConsumer;
    private QueueBuilder requestQueueBuilder;
    private QueueBuilder webSocketMessageQueueBuilder;
    private QBitSystemManager qBitSystemManager;


    public QBitSystemManager getSystemManager() {
        return qBitSystemManager;
    }

    public HttpServerBuilder setSystemManager(QBitSystemManager qBitSystemManager) {
        this.qBitSystemManager = qBitSystemManager;
        return this;
    }


    public QueueBuilder getRequestQueueBuilder() {
        return requestQueueBuilder;
    }

    public HttpServerBuilder setRequestQueueBuilder(QueueBuilder requestQueueBuilder) {
        this.requestQueueBuilder = requestQueueBuilder;
        return this;
    }

    public QueueBuilder getWebSocketMessageQueueBuilder() {
        return webSocketMessageQueueBuilder;
    }

    public HttpServerBuilder setWebSocketMessageQueueBuilder(QueueBuilder webSocketMessageQueueBuilder) {
        this.webSocketMessageQueueBuilder = webSocketMessageQueueBuilder;
        return this;
    }

    public int getMaxRequestBatches() {
        return maxRequestBatches;
    }



    public HttpServerBuilder setMaxRequestBatches(int maxRequestBatches) {
        this.maxRequestBatches = maxRequestBatches;
        return this;
    }

    public int getWorkers() {
        return workers;
    }

    public HttpServerBuilder setWorkers(int workers) {
        this.workers = workers;
        return this;
    }

    public Class<Consumer> getHandlerClass() {
        return handlerClass;
    }

    public HttpServerBuilder setHandlerClass(Class handlerClass) {
        this.handlerClass = handlerClass;
        return this;
    }

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

        final HttpServer httpServer;

        if (getWorkers() == -1 || getHandlerClass()==null) {
            httpServer = QBit.factory().createHttpServer(this.getHost(),
                    this.getPort(), this.isManageQueues(), this.getPollTime(), this.getRequestBatchSize(),
                    this.getFlushInterval(), this.getMaxRequestBatches(), this.getSystemManager());

            httpServer.setHttpRequestConsumer(this.getHttpRequestConsumer());
            httpServer.setWebSocketMessageConsumer(this.getWebSocketMessageConsumer());
        } else {

            if (webSocketMessageQueueBuilder!=null || requestQueueBuilder!=null) {

                if (webSocketMessageQueueBuilder == null) {
                    webSocketMessageQueueBuilder = requestQueueBuilder;
                }
                if (requestQueueBuilder == null) {
                    requestQueueBuilder = webSocketMessageQueueBuilder;

                }

                final Queue<HttpRequest> requestQueue = requestQueueBuilder.build();
                final Queue<WebSocketMessage> webSocketMessageQueue = webSocketMessageQueueBuilder.build();
                httpServer = QBit.factory().createHttpServerWithQueue(this.getHost(),
                        this.getPort(), this.getFlushInterval(),
                        requestQueue, webSocketMessageQueue, getSystemManager());
                httpServer.setHttpRequestConsumer(this.getHttpRequestConsumer());
                httpServer.setWebSocketMessageConsumer(this.getWebSocketMessageConsumer());

            } else {

                httpServer = QBit.factory().createHttpServerWithWorkers(this.getHost(),
                        this.getPort(), this.isManageQueues(), this.getPollTime(), this.getRequestBatchSize(),
                        this.getFlushInterval(), this.getMaxRequestBatches(), this.getWorkers(),
                        this.getHandlerClass(), this.getSystemManager());

                httpServer.setHttpRequestConsumer(this.getHttpRequestConsumer());
                httpServer.setWebSocketMessageConsumer(this.getWebSocketMessageConsumer());
                return httpServer;
            }

        }


        if (httpServer!=null && qBitSystemManager!=null) {
            qBitSystemManager.registerServer(httpServer);
        }
        return httpServer;
    }

}
