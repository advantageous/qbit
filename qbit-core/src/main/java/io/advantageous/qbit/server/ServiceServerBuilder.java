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

package io.advantageous.qbit.server;

import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.http.server.HttpServer;
import io.advantageous.qbit.json.JsonMapper;
import io.advantageous.qbit.message.Request;
import io.advantageous.qbit.queue.QueueBuilder;
import io.advantageous.qbit.service.BeforeMethodCall;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.service.impl.ServiceConstants;
import io.advantageous.qbit.spi.ProtocolEncoder;
import io.advantageous.qbit.spi.ProtocolParser;
import io.advantageous.qbit.system.QBitSystemManager;
import io.advantageous.qbit.transforms.Transformer;

import static io.advantageous.qbit.http.server.HttpServerBuilder.httpServerBuilder;

/**
 * Allows for the programmatic construction of a service.
 *
 * @author rhightower
 *         Created by Richard on 11/14/14.
 */

public class ServiceServerBuilder {

    private String host;
    private int port = 8080;
    private boolean manageQueues = false;
    private int pollTime = GlobalConstants.POLL_WAIT;
    private int requestBatchSize = GlobalConstants.BATCH_SIZE;
    private int flushInterval = 200;
    private String uri = "/services";
    private int numberOfOutstandingRequests = 1_000_000;
    private int maxRequestBatches = 10_000;
    private int timeoutSeconds = 30;
    private boolean invokeDynamic = true;
    private QueueBuilder requestQueueBuilder;
    private QueueBuilder webSocketMessageQueueBuilder;
    private QueueBuilder serviceBundleQueueBuilder;
    private boolean eachServiceInItsOwnThread = true;
    private HttpServer httpServer;
    private QBitSystemManager qBitSystemManager;
    /**
     * Allows interception of method calls before they get sent to a client.
     * This allows us to transform or reject method calls.
     */
    private BeforeMethodCall beforeMethodCall = ServiceConstants.NO_OP_BEFORE_METHOD_CALL;
    /**
     * Allows interception of method calls before they get transformed and sent to a client.
     * This allows us to transform or reject method calls.
     */
    private BeforeMethodCall beforeMethodCallAfterTransform = ServiceConstants.NO_OP_BEFORE_METHOD_CALL;
    /**
     * Allows transformation of arguments, for example from JSON to Java objects.
     */
    private Transformer<Request, Object> argTransformer = ServiceConstants.NO_OP_ARG_TRANSFORM;

    public static ServiceServerBuilder serviceServerBuilder() {
        return new ServiceServerBuilder();
    }

    public QBitSystemManager getSystemManager() {
        return qBitSystemManager;
    }

    public ServiceServerBuilder setSystemManager(QBitSystemManager qBitSystemManager) {
        this.qBitSystemManager = qBitSystemManager;
        return this;
    }

    public HttpServer getHttpServer() {
        return httpServer;
    }

    public ServiceServerBuilder setHttpServer(HttpServer httpServer) {
        this.httpServer = httpServer;
        return this;
    }

    public QueueBuilder getRequestQueueBuilder() {
        return requestQueueBuilder;
    }

    public ServiceServerBuilder setRequestQueueBuilder(QueueBuilder requestQueueBuilder) {
        this.requestQueueBuilder = requestQueueBuilder;
        return this;
    }

    public QueueBuilder getWebSocketMessageQueueBuilder() {
        return webSocketMessageQueueBuilder;
    }

    public ServiceServerBuilder setWebSocketMessageQueueBuilder(QueueBuilder webSocketMessageQueueBuilder) {
        this.webSocketMessageQueueBuilder = webSocketMessageQueueBuilder;
        return this;
    }

    public boolean isInvokeDynamic() {
        return invokeDynamic;
    }

    public ServiceServerBuilder setInvokeDynamic(boolean invokeDynamic) {
        this.invokeDynamic = invokeDynamic;
        return this;
    }

    public int getMaxRequestBatches() {
        return maxRequestBatches;
    }

    public ServiceServerBuilder setMaxRequestBatches(int maxRequestBatches) {
        this.maxRequestBatches = maxRequestBatches;
        return this;
    }

    public boolean isEachServiceInItsOwnThread() {
        return eachServiceInItsOwnThread;
    }

    public ServiceServerBuilder setEachServiceInItsOwnThread(boolean eachServiceInItsOwnThread) {
        this.eachServiceInItsOwnThread = eachServiceInItsOwnThread;
        return this;
    }

    public BeforeMethodCall getBeforeMethodCall() {
        return beforeMethodCall;
    }

    public ServiceServerBuilder setBeforeMethodCall(BeforeMethodCall beforeMethodCall) {
        this.beforeMethodCall = beforeMethodCall;
        return this;
    }

    public BeforeMethodCall getBeforeMethodCallAfterTransform() {
        return beforeMethodCallAfterTransform;
    }

    public ServiceServerBuilder setBeforeMethodCallAfterTransform(BeforeMethodCall beforeMethodCallAfterTransform) {
        this.beforeMethodCallAfterTransform = beforeMethodCallAfterTransform;
        return this;
    }

    public Transformer<Request, Object> getArgTransformer() {
        return argTransformer;

    }

    public ServiceServerBuilder setArgTransformer(Transformer<Request, Object> argTransformer) {
        this.argTransformer = argTransformer;
        return this;
    }

    public int getNumberOfOutstandingRequests() {
        return numberOfOutstandingRequests;
    }

    public ServiceServerBuilder setNumberOfOutstandingRequests(int numberOfOutstandingRequests) {
        this.numberOfOutstandingRequests = numberOfOutstandingRequests;
        return this;
    }

    public String getUri() {
        return uri;
    }

    public ServiceServerBuilder setUri(String uri) {
        this.uri = uri;
        return this;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public ServiceServerBuilder setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
        return this;
    }

    public String getHost() {
        return host;
    }

    public ServiceServerBuilder setHost(String host) {
        this.host = host;
        return this;
    }

    public int getPort() {
        return port;
    }

    public ServiceServerBuilder setPort(int port) {
        this.port = port;
        return this;
    }

    public boolean isManageQueues() {
        return manageQueues;
    }

    public ServiceServerBuilder setManageQueues(boolean manageQueues) {
        this.manageQueues = manageQueues;
        return this;
    }

    public int getPollTime() {
        return pollTime;
    }

    public ServiceServerBuilder setPollTime(int pollTime) {
        this.pollTime = pollTime;
        return this;
    }

    public int getRequestBatchSize() {
        return requestBatchSize;
    }

    public ServiceServerBuilder setRequestBatchSize(int requestBatchSize) {
        this.requestBatchSize = requestBatchSize;
        return this;
    }

    public int getFlushInterval() {
        return flushInterval;
    }

    public ServiceServerBuilder setFlushInterval(int flushInterval) {
        this.flushInterval = flushInterval;
        return this;
    }

    public QueueBuilder getServiceBundleQueueBuilder() {
        return serviceBundleQueueBuilder;
    }

    public ServiceServerBuilder setServiceBundleQueueBuilder(QueueBuilder serviceBundleQueueBuilder) {
        this.serviceBundleQueueBuilder = serviceBundleQueueBuilder;
        return this;
    }

    public ServiceServer build() {

        if (httpServer == null) {
            httpServer = createHttpServer();
        }

        final JsonMapper jsonMapper = QBit.factory().createJsonMapper();
        final ProtocolEncoder encoder = QBit.factory().createEncoder();
        if (serviceBundleQueueBuilder == null) {
            serviceBundleQueueBuilder = new QueueBuilder().setBatchSize(this.getRequestBatchSize()).setPollWait(this.getPollTime());
        }
        final ServiceBundle serviceBundle = QBit.factory().createServiceBundle(uri,
                serviceBundleQueueBuilder,
                QBit.factory(),
                eachServiceInItsOwnThread, this.getBeforeMethodCall(),
                this.getBeforeMethodCallAfterTransform(),
                this.getArgTransformer(), true, getSystemManager());
        final ProtocolParser parser = QBit.factory().createProtocolParser();
        final ServiceServer serviceServer = QBit.factory().createServiceServer(httpServer,
                encoder, parser, serviceBundle, jsonMapper, this.getTimeoutSeconds(),
                this.getNumberOfOutstandingRequests(), this.getRequestBatchSize(),
                this.getFlushInterval(), this.getSystemManager());


        if (serviceServer != null && qBitSystemManager != null) {
            qBitSystemManager.registerServer(serviceServer);
        }
        return serviceServer;
    }

    private HttpServer createHttpServer() {

        return httpServerBuilder().setPort(port)
                .setHost(host)
                .setManageQueues(this.isManageQueues())
                .setFlushInterval(this.getFlushInterval())
                .setPollTime(this.getPollTime())
                .setRequestBatchSize(this.getRequestBatchSize())
                .setMaxRequestBatches(this.getMaxRequestBatches())
                .setRequestQueueBuilder(this.getRequestQueueBuilder())
                .setSystemManager(getSystemManager())
                .setWebSocketMessageQueueBuilder(this.getWebSocketMessageQueueBuilder()).build();
    }
}
