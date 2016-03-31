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

package io.advantageous.qbit.client;

import io.advantageous.qbit.Factory;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.config.PropertyResolver;
import io.advantageous.qbit.http.client.HttpClientBuilder;
import io.advantageous.qbit.service.discovery.EndpointDefinition;
import io.advantageous.qbit.service.discovery.ServiceDiscovery;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Client builder is used to create a client programatically.
 */
public class ClientBuilder {


    public static final String QBIT_CLIENT_BUILDER = "qbit.client.builder.";

    private int protocolBatchSize = 80;
    private String uri;
    private HttpClientBuilder httpClientBuilder;
    private ServiceDiscovery serviceDiscovery;
    private Factory factory;
    private String serviceName;

    private BeforeMethodSent beforeMethodSent;

    public ClientBuilder(PropertyResolver propertyResolver) {

        HttpClientBuilder httpClientBuilder = getHttpClientBuilder();
        httpClientBuilder.setAutoFlush(propertyResolver.getBooleanProperty("autoFlush", true));
        httpClientBuilder.setHost(propertyResolver.getStringProperty("host", "localhost"));
        httpClientBuilder.setPort(propertyResolver.getIntegerProperty("port", 8080));
        httpClientBuilder.setPoolSize(propertyResolver.getIntegerProperty("poolSize", 1));
        httpClientBuilder.setKeepAlive(propertyResolver.getBooleanProperty("keepAlive", true));
        httpClientBuilder.setPipeline(propertyResolver.getBooleanProperty("pipeline", true));
        httpClientBuilder.setTimeOutInMilliseconds(propertyResolver.getIntegerProperty("timeOutInMilliseconds", 3000));
        this.protocolBatchSize = propertyResolver.getIntegerProperty("protocolBatchSize", protocolBatchSize);
        httpClientBuilder.setTimeOutInMilliseconds(propertyResolver.getIntegerProperty("flushInterval", 500));
        this.uri = propertyResolver.getStringProperty("uri", "/services");
        httpClientBuilder.setTimeOutInMilliseconds(propertyResolver.getIntegerProperty("timeoutSeconds", 30) * 1000);

    }

    public ClientBuilder() {
        this(PropertyResolver.createSystemPropertyResolver(QBIT_CLIENT_BUILDER));
    }

    public ClientBuilder(final Properties properties) {
        this(PropertyResolver.createPropertiesPropertyResolver(
                QBIT_CLIENT_BUILDER, properties));
    }

    public static ClientBuilder clientBuilder() {
        return new ClientBuilder();
    }

    public Factory getFactory() {
        if (factory == null) {
            factory = QBit.factory();
        }
        return factory;
    }

    public ClientBuilder setFactory(Factory factory) {
        this.factory = factory;
        return this;
    }

    public BeforeMethodSent getBeforeMethodSent() {
        if (beforeMethodSent == null) {
            beforeMethodSent = new BeforeMethodSent() {
            };
        }
        return beforeMethodSent;
    }

    public ClientBuilder setBeforeMethodSent(BeforeMethodSent beforeMethodSent) {
        this.beforeMethodSent = beforeMethodSent;
        return this;
    }

    public int getTimeOutInMilliseconds() {
        return getHttpClientBuilder().getTimeOutInMilliseconds();
    }

    public ClientBuilder setTimeOutInMilliseconds(int timeOutInMilliseconds) {
        getHttpClientBuilder().setTimeOutInMilliseconds(timeOutInMilliseconds);
        return this;
    }

    public int getPoolSize() {
        return getHttpClientBuilder().getPoolSize();
    }

    public ClientBuilder setPoolSize(int poolSize) {

        getHttpClientBuilder().setPoolSize(poolSize);
        return this;
    }

    public boolean isKeepAlive() {
        return getHttpClientBuilder().isKeepAlive();
    }

    public ClientBuilder setKeepAlive(boolean keepAlive) {
        getHttpClientBuilder().setKeepAlive(keepAlive);
        return this;
    }

    public boolean isPipeline() {
        return getHttpClientBuilder().isPipeline();
    }

    public ClientBuilder setPipeline(boolean pipeline) {
        getHttpClientBuilder().setPipeline(pipeline);
        return this;
    }

    public String getUri() {
        return uri;
    }

    public ClientBuilder setUri(String uri) {
        this.uri = uri;
        return this;
    }

    public int getTimeoutSeconds() {

        int timeOutInMilliseconds = getHttpClientBuilder().getTimeOutInMilliseconds();
        if (timeOutInMilliseconds > 0) {
            return timeOutInMilliseconds / 1000;
        } else {
            return 0;
        }

    }

    public ClientBuilder setTimeoutSeconds(int timeoutSeconds) {

        getHttpClientBuilder().setTimeOutInMilliseconds(timeoutSeconds * 1000);
        return this;
    }

    public String getHost() {

        return getHttpClientBuilder().getHost();

    }

    public ClientBuilder setHost(String host) {
        getHttpClientBuilder().setHost(host);
        return this;
    }

    public ClientBuilder setHostAndPort(final InetSocketAddress inetSocketAddress) {
        this.setHost(inetSocketAddress.getHostName());
        this.setPort(inetSocketAddress.getPort());
        return this;
    }


    public ClientBuilder setHostAndPort(final URI uri) {
        this.setHost(uri.getHost());
        this.setPort(uri.getPort());
        return this;
    }


    public ClientBuilder setHostAndPort(final URL url) {
        this.setHost(url.getHost());
        this.setPort(url.getPort());
        return this;
    }

    public int getPort() {
        return getHttpClientBuilder().getPort();
    }

    public ClientBuilder setPort(int port) {
        getHttpClientBuilder().setPort(port);
        return this;
    }

    public boolean isAutoFlush() {
        return getHttpClientBuilder().isAutoFlush();
    }

    public ClientBuilder setAutoFlush(boolean autoFlush) {
        getHttpClientBuilder().setAutoFlush(autoFlush);
        return this;
    }


    public int getFlushInterval() {
        return getHttpClientBuilder().getFlushInterval();
    }

    public ClientBuilder setFlushInterval(int flushInterval) {
        getHttpClientBuilder().setFlushInterval(flushInterval);
        return this;
    }


    public int getProtocolBatchSize() {
        return protocolBatchSize;
    }

    public ClientBuilder setProtocolBatchSize(int protocolBatchSize) {
        this.protocolBatchSize = protocolBatchSize;
        return this;
    }


    public HttpClientBuilder getHttpClientBuilder() {
        if (httpClientBuilder == null) {
            httpClientBuilder = HttpClientBuilder.httpClientBuilder();
        }
        return httpClientBuilder;
    }

    public void setHttpClientBuilder(HttpClientBuilder httpClientBuilder) {
        this.httpClientBuilder = httpClientBuilder;
    }

    public ClientBuilder setServiceDiscovery(ServiceDiscovery serviceDiscovery, String serviceName) {
        this.serviceDiscovery = serviceDiscovery;
        this.setServiceName(serviceName);
        return this;
    }

    public ServiceDiscovery getServiceDiscovery() {
        return serviceDiscovery;
    }

    public String getServiceName() {
        return serviceName;
    }

    public ClientBuilder setServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public Client build() {

        /**
         * String host, int port, int pollTime, int requestBatchSize, int timeOutInMilliseconds, int poolSize, boolean autoFlush
         */

        final HttpClientBuilder httpClientBuilder = getHttpClientBuilder();


        final ServiceDiscovery serviceDiscovery = getServiceDiscovery();

        if (serviceDiscovery != null && getServiceName() != null) {
            List<EndpointDefinition> endpointDefinitions = serviceDiscovery.loadServices(getServiceName());
            if (endpointDefinitions == null || endpointDefinitions.size() == 0) {
                endpointDefinitions = serviceDiscovery.loadServicesNow(getServiceName());
            }

            if (endpointDefinitions != null && endpointDefinitions.size() > 0) {

                endpointDefinitions = new ArrayList<>(endpointDefinitions);
                Collections.shuffle(endpointDefinitions);
                final EndpointDefinition endpointDefinition = endpointDefinitions.get(0);

                httpClientBuilder.setPort(endpointDefinition.getPort()).setHost(endpointDefinition.getHost());
            }
        }


        //noinspection UnnecessaryLocalVariable
        @SuppressWarnings("UnnecessaryLocalVariable")
        Client client = getFactory().createClient(getUri(), httpClientBuilder.build(), getProtocolBatchSize(), getBeforeMethodSent());
        return client;

    }

}
