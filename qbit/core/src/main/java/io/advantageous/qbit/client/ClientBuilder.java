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

import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.config.PropertyResolver;
import io.advantageous.qbit.http.client.HttpClient;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.util.Properties;

/**
 * Client builder is used to createWithWorkers a client programatically.
 */
public class ClientBuilder {


    public static final String QBIT_CLIENT_BUILDER = "qbit.client.builder.";
    private String host;
    private int port;
    private boolean autoFlush;
    private int pollTime;
    private int poolSize;
    private int requestBatchSize;
    private boolean keepAlive;
    private boolean pipeline;
    private int timeOutInMilliseconds;
    private int protocolBatchSize;
    private int flushInterval;
    private String uri;
    private int timeoutSeconds = 30;

    public ClientBuilder(PropertyResolver propertyResolver) {
        this.autoFlush = propertyResolver.getBooleanProperty("autoFlush", true);
        this.host = propertyResolver.getStringProperty("host", "localhost");
        this.port = propertyResolver.getIntegerProperty("port", 8080);
        this.poolSize = propertyResolver.getIntegerProperty("poolSize", 1);
        this.pollTime = propertyResolver.getIntegerProperty("pollTime", GlobalConstants.POLL_WAIT);
        this.requestBatchSize = propertyResolver
                .getIntegerProperty("requestBatchSize", GlobalConstants.BATCH_SIZE);
        this.keepAlive = propertyResolver.getBooleanProperty("keepAlive", true);
        this.pipeline = propertyResolver.getBooleanProperty("pipeline", true);
        this.timeOutInMilliseconds = propertyResolver.getIntegerProperty("timeOutInMilliseconds", 3000);
        this.protocolBatchSize = propertyResolver.getIntegerProperty("protocolBatchSize", -1);
        this.flushInterval = propertyResolver.getIntegerProperty("flushInterval", 500);
        this.uri = propertyResolver.getStringProperty("uri", "/services");
        this.timeoutSeconds = propertyResolver.getIntegerProperty("timeoutSeconds", 30);
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

    public int getTimeOutInMilliseconds() {
        return timeOutInMilliseconds;
    }

    public ClientBuilder setTimeOutInMilliseconds(int timeOutInMilliseconds) {
        this.timeOutInMilliseconds = timeOutInMilliseconds;
        return this;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public ClientBuilder setPoolSize(int poolSize) {
        this.poolSize = poolSize;
        return this;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public ClientBuilder setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
        return this;
    }

    public boolean isPipeline() {
        return pipeline;
    }

    public ClientBuilder setPipeline(boolean pipeline) {
        this.pipeline = pipeline;
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
        return timeoutSeconds;
    }

    public ClientBuilder setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
        return this;
    }

    public String getHost() {
        return host;
    }

    public ClientBuilder setHost(String host) {
        this.host = host;
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

        final HttpClient httpClient = QBit.factory().createHttpClient(
                this.getHost(),
                this.getPort(),
                this.getRequestBatchSize(),
                this.getTimeOutInMilliseconds(),
                this.getPoolSize(),
                this.isAutoFlush(),
                this.getFlushInterval(),
                this.isKeepAlive(), this.isPipeline());

        if (protocolBatchSize == -1) {
            protocolBatchSize = requestBatchSize;
        }

        //noinspection UnnecessaryLocalVariable
        @SuppressWarnings("UnnecessaryLocalVariable")
        Client client = QBit.factory().createClient(uri, httpClient, protocolBatchSize);
        return client;

    }

}
