/*
 * Copyright (c) 2015. Rick Hightower, Geoff Chandler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
 */

package io.advantageous.qbit.http.server;

import io.advantageous.qbit.QBit;
import io.advantageous.qbit.http.config.HttpServerConfig;
import io.advantageous.qbit.queue.QueueBuilder;
import io.advantageous.qbit.system.QBitSystemManager;

import java.util.function.Consumer;

/**
 * Allows one to build().start() an HTTP server.
 *
 * @author rhightower
 *         Created by Richard on 11/12/14.
 */
public class HttpServerBuilder {

    private HttpServerConfig httpServerConfig = new HttpServerConfig();
    private QueueBuilder requestQueueBuilder;
    private QueueBuilder responseQueueBuilder;
    private QueueBuilder webSocketMessageQueueBuilder;
    private QBitSystemManager qBitSystemManager;

    public static HttpServerBuilder httpServerBuilder() {
        return new HttpServerBuilder();
    }

    public QBitSystemManager getSystemManager() {
        return qBitSystemManager;
    }

    public HttpServerBuilder setSystemManager(QBitSystemManager qBitSystemManager) {
        this.qBitSystemManager = qBitSystemManager;
        return this;
    }

    public HttpServerConfig getHttpServerConfig() {
        return httpServerConfig;
    }

    public HttpServerBuilder setHttpServerConfig(HttpServerConfig httpServerConfig) {
        this.httpServerConfig = httpServerConfig;
        return this;
    }


    public QueueBuilder getResponseQueueBuilder() {
        return responseQueueBuilder;
    }

    public HttpServerBuilder setResponseQueueBuilder(QueueBuilder responseQueueBuilder) {
        this.responseQueueBuilder = responseQueueBuilder;
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
        return httpServerConfig.getMaxRequestBatches();
    }


    public HttpServerBuilder setMaxRequestBatches(int maxRequestBatches) {
        this.httpServerConfig.setMaxRequestBatches(maxRequestBatches);
        return this;
    }

    public int getWorkers() {
        return httpServerConfig.getWorkers();
    }

    public HttpServerBuilder setWorkers(int workers) {
        this.httpServerConfig.setWorkers(workers);
        return this;
    }


    public boolean isPipeline() {

        return this.httpServerConfig.isPipeline();
    }

    public HttpServerBuilder setPipeline(boolean pipeline) {
        this.httpServerConfig.setPipeline(pipeline);
        return this;
    }

    public String getHost() {

        return this.httpServerConfig.getHost();
    }

    public HttpServerBuilder setHost(String host) {
        this.httpServerConfig.setHost(host);
        return this;
    }

    public int getPort() {
        return this.httpServerConfig.getPort();
    }

    public HttpServerBuilder setPort(int port) {
        this.httpServerConfig.setPort(port);
        return this;
    }

    public boolean isManageQueues() {
        return this.httpServerConfig.isManageQueues();
    }

    public HttpServerBuilder setManageQueues(boolean manageQueues) {
        this.httpServerConfig.setManageQueues(manageQueues);
        return this;
    }

    public int getPollTime() {
        return this.httpServerConfig.getPollTime();
    }

    public HttpServerBuilder setPollTime(int pollTime) {
        this.httpServerConfig.setPollTime(pollTime);
        return this;
    }

    public int getRequestBatchSize() {
        return this.httpServerConfig.getRequestBatchSize();
    }

    public HttpServerBuilder setRequestBatchSize(int requestBatchSize) {
        this.httpServerConfig.setRequestBatchSize(requestBatchSize);
        return this;
    }

    public int getFlushInterval() {
        return this.httpServerConfig.getFlushInterval();
    }

    public HttpServerBuilder setFlushInterval(int flushInterval) {
        this.httpServerConfig.setFlushInterval(flushInterval);
        return this;
    }

    public HttpServerConfig getConfig() {
        return httpServerConfig;
    }

    public HttpServerBuilder setConfig(HttpServerConfig config) {
        this.httpServerConfig = config;
        return this;
    }

    public HttpServerBuilder withConfig(Consumer<HttpServerConfig> config) {
        config.accept(this.httpServerConfig);
        return this;
    }

    public HttpServer build() {

        final HttpServer httpServer = QBit.factory().createHttpServer(
                this.getConfig(), this.getRequestQueueBuilder(), this.getResponseQueueBuilder(),
                this.getWebSocketMessageQueueBuilder(),
                getSystemManager());

        if (qBitSystemManager != null) {
            qBitSystemManager.registerServer(httpServer);
        }
        return httpServer;
    }

}
