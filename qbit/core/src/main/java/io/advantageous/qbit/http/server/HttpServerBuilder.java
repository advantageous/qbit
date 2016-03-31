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
import io.advantageous.qbit.http.config.HttpServerConfig;
import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.request.HttpResponseCreator;
import io.advantageous.qbit.http.request.decorator.HttpResponseDecorator;
import io.advantageous.qbit.http.request.impl.HttpResponseCreatorDefault;
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

    private HttpServerConfig httpServerConfig;
    private QBitSystemManager qBitSystemManager;
    private ServiceDiscovery serviceDiscovery;
    private HealthServiceAsync healthServiceAsync;
    private Factory factory;
    private String endpointName;
    private int serviceDiscoveryTtl = 60;
    private TimeUnit serviceDiscoveryTtlTimeUnit = TimeUnit.SECONDS;
    private RequestContinuePredicate requestContinuePredicate = null;
    private RequestContinuePredicate requestBodyContinuePredicate = null;


    private CopyOnWriteArrayList<HttpResponseDecorator> responseDecorators = new CopyOnWriteArrayList<>();
    private HttpResponseCreator httpResponseCreator = new HttpResponseCreatorDefault();

    public static HttpServerBuilder httpServerBuilder() {
        return new HttpServerBuilder();
    }

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

    public RequestContinuePredicate getRequestBodyContinuePredicate() {
        if (requestBodyContinuePredicate == null) {
            requestBodyContinuePredicate = new RequestContinuePredicate();
        }
        return requestBodyContinuePredicate;
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

    public HttpServerBuilder addRequestBodyContinuePredicate(final Predicate<HttpRequest> predicate) {
        getRequestBodyContinuePredicate().add(predicate);
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

    public QBitSystemManager getSystemManager() {
        return qBitSystemManager;
    }

    public HttpServerBuilder setSystemManager(QBitSystemManager qBitSystemManager) {
        this.qBitSystemManager = qBitSystemManager;
        return this;
    }

    public HttpServerConfig getHttpServerConfig() {
        if (httpServerConfig == null) {
            httpServerConfig = new HttpServerConfig();
            httpServerConfig.setFlushInterval(50);
        }
        return httpServerConfig;
    }

    public HttpServerBuilder setHttpServerConfig(HttpServerConfig httpServerConfig) {
        this.httpServerConfig = httpServerConfig;
        return this;
    }


    public int getWorkers() {
        return getHttpServerConfig().getWorkers();
    }

    public HttpServerBuilder setWorkers(int workers) {
        this.getHttpServerConfig().setWorkers(workers);
        return this;
    }


    public boolean isPipeline() {

        return this.getHttpServerConfig().isPipeline();
    }

    public HttpServerBuilder setPipeline(boolean pipeline) {
        this.getHttpServerConfig().setPipeline(pipeline);
        return this;
    }

    public String getHost() {

        return this.getHttpServerConfig().getHost();
    }

    public HttpServerBuilder setHost(String host) {
        this.getHttpServerConfig().setHost(host);
        return this;
    }

    public int getPort() {
        return this.getHttpServerConfig().getPort();
    }

    public HttpServerBuilder setPort(int port) {
        this.getHttpServerConfig().setPort(port);
        return this;
    }

    public int getFlushInterval() {
        return this.getHttpServerConfig().getFlushInterval();
    }

    public HttpServerBuilder setFlushInterval(int flushInterval) {
        this.getHttpServerConfig().setFlushInterval(flushInterval);
        return this;
    }

    public HttpServerConfig getConfig() {
        if (httpServerConfig == null) {
            httpServerConfig = new HttpServerConfig();
        }
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
                this.getHttpResponseCreator(),
                this.getRequestBodyContinuePredicate()
        );

        if (requestContinuePredicate != null) {
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
