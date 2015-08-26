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

import io.advantageous.qbit.Factory;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.http.request.HttpResponseCreator;
import io.advantageous.qbit.http.request.impl.HttpResponseCreatorDefault;
import io.advantageous.qbit.http.request.HttpResponseDecorator;
import io.advantageous.qbit.http.config.HttpServerConfig;
import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.service.discovery.ServiceDiscovery;
import io.advantageous.qbit.service.health.HealthServiceAsync;
import io.advantageous.qbit.system.QBitSystemManager;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Allows one to build an HTTP server.
 *
 * @author rhightower
 *         created by Richard on 11/12/14.
 */
public class HttpServerBuilder {

    private HttpServerConfig httpServerConfig = new HttpServerConfig();
    private QBitSystemManager qBitSystemManager;
    private ServiceDiscovery serviceDiscovery;
    private HealthServiceAsync healthServiceAsync;
    private Factory factory;
    private String endpointName;
    private int serviceDiscoveryTtl = 60;
    private TimeUnit serviceDiscoveryTtlTimeUnit = TimeUnit.SECONDS;
    private RequestContinuePredicate requestContinuePredicate = null;
    private CopyOnWriteArrayList<HttpResponseDecorator> responseDecorators = new CopyOnWriteArrayList<>();
    private HttpResponseCreator httpResponseCreator = new HttpResponseCreatorDefault();

    public CopyOnWriteArrayList<HttpResponseDecorator> getResponseDecorators() {
        return responseDecorators;
    }

    public HttpServerBuilder setResponseDecorators(CopyOnWriteArrayList<HttpResponseDecorator> decorators) {
        this.responseDecorators = decorators;
        return this;
    }


    public HttpServerBuilder addResponseDecorator(final HttpResponseDecorator decorator) {
        responseDecorators.add(decorator);
        return this;
    }

    public HttpResponseCreator getHttpResponseCreator() {
        return httpResponseCreator;
    }

    public HttpServerBuilder setHttpResponseCreator(HttpResponseCreator httpResponseCreator) {
        this.httpResponseCreator = httpResponseCreator;
        return null;
    }



    public RequestContinuePredicate getRequestContinuePredicate() {
        if (requestContinuePredicate == null) {
            requestContinuePredicate = new RequestContinuePredicate();
        }
        return requestContinuePredicate;
    }

    public HttpServerBuilder setRequestContinuePredicate(final RequestContinuePredicate requestContinuePredicate) {
        this.requestContinuePredicate = requestContinuePredicate;
        return this;
    }

    public HttpServerBuilder addShouldContinueHttpRequestPredicate(final Predicate<HttpRequest> predicate) {
        getRequestContinuePredicate().add(predicate);
        return this;
    }


    public ServiceDiscovery getServiceDiscovery() {
        return serviceDiscovery;
    }

    public HttpServerBuilder setServiceDiscovery(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
        return this;
    }

    public HealthServiceAsync getHealthServiceAsync() {
        return healthServiceAsync;
    }

    public HttpServerBuilder setHealthServiceAsync(HealthServiceAsync healthServiceAsync) {
        this.healthServiceAsync = healthServiceAsync;
        return this;
    }

    public Factory getFactory() {
        if (factory == null) {
            factory = QBit.factory();
        }
        return factory;
    }

    public HttpServerBuilder setFactory(Factory factory) {
        this.factory = factory;
        return this;
    }

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

        final HttpServer httpServer = getFactory().createHttpServer(
                this.getConfig(),
                this.getEndpointName(),
                this.getSystemManager(),
                this.getServiceDiscovery(),
                this.getHealthServiceAsync(),
                this.getServiceDiscoveryTtl(),
                this.getServiceDiscoveryTtlTimeUnit(),
                this.getResponseDecorators(),
                this.getHttpResponseCreator()
        );

        if (requestContinuePredicate!=null) {
            httpServer.setShouldContinueHttpRequest(requestContinuePredicate);
        }

        if (qBitSystemManager != null) {
            qBitSystemManager.registerServer(httpServer);
        }
        return httpServer;
    }

    public String getEndpointName() {
        return endpointName;
    }

    public HttpServerBuilder setEndpointName(String endpointName) {
        this.endpointName = endpointName;
        return this;
    }

    public int getServiceDiscoveryTtl() {
        return serviceDiscoveryTtl;
    }

    public HttpServerBuilder setServiceDiscoveryTtl(int serviceDiscoveryTtl) {
        this.serviceDiscoveryTtl = serviceDiscoveryTtl;
        return this;
    }

    public TimeUnit getServiceDiscoveryTtlTimeUnit() {
        return serviceDiscoveryTtlTimeUnit;
    }

    public HttpServerBuilder setServiceDiscoveryTtlTimeUnit(TimeUnit serviceDiscoveryTtlTimeUnit) {
        this.serviceDiscoveryTtlTimeUnit = serviceDiscoveryTtlTimeUnit;
        return this;
    }
}
