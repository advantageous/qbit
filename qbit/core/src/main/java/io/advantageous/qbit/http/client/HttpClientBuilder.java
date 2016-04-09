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

package io.advantageous.qbit.http.client;

import io.advantageous.qbit.QBit;
import io.advantageous.qbit.config.PropertyResolver;

import java.util.Properties;
import java.util.function.Consumer;


/**
 * This allows one to construct an http client which attaches to a remote server.
 *
 * @author rhightower
 *         created by rhightower on 11/13/14.
 */
public class HttpClientBuilder {


    public static final String QBIT_HTTP_CLIENT_BUILDER = "qbit.http.client.builder.";

    private String host = "localhost";
    private int port = 8080;
    private int poolSize = 1;
    private int timeOutInMilliseconds = 3_000;
    private boolean autoFlush = true;
    private boolean keepAlive = true;
    private boolean pipeline = false;
    private int flushInterval = 50;
    private boolean ssl = false;
    private boolean verifyHost = false;
    private boolean trustAll = true;
    private int maxWebSocketFrameSize = 100_000_000;
    private boolean tryUseCompression;
    private String trustStorePath;
    private String trustStorePassword;
    private boolean tcpNoDelay = true;
    private int soLinger = 100;
    private Consumer<Throwable> errorHandler = throwable -> {
    };


    public HttpClientBuilder(PropertyResolver propertyResolver) {
        this.autoFlush = propertyResolver.getBooleanProperty("autoFlush", autoFlush);
        this.host = propertyResolver.getStringProperty("host", host);
        this.port = propertyResolver.getIntegerProperty("port", port);
        this.poolSize = propertyResolver.getIntegerProperty("poolSize", poolSize);
        this.keepAlive = propertyResolver.getBooleanProperty("keepAlive", keepAlive);
        this.pipeline = propertyResolver.getBooleanProperty("pipeline", pipeline);
        this.timeOutInMilliseconds = propertyResolver.getIntegerProperty("timeOutInMilliseconds", timeOutInMilliseconds);
        this.flushInterval = propertyResolver.getIntegerProperty("flushInterval", flushInterval);
        this.soLinger = propertyResolver.getIntegerProperty("soLinger", soLinger);
        this.ssl = propertyResolver.getBooleanProperty("ssl", ssl);
        this.verifyHost = propertyResolver.getBooleanProperty("verifyHost", verifyHost);
        this.trustAll = propertyResolver.getBooleanProperty("trustAll", trustAll);
        this.tryUseCompression = propertyResolver.getBooleanProperty("tryUseCompression", tryUseCompression);
        this.tcpNoDelay = propertyResolver.getBooleanProperty("tcpNoDelay", tcpNoDelay);
        this.trustStorePath = propertyResolver.getStringProperty("trustStorePath", trustStorePath);
        this.trustStorePassword = propertyResolver.getStringProperty("trustStorePassword", trustStorePassword);


    }


    public HttpClientBuilder() {
        this(PropertyResolver.createSystemPropertyResolver(QBIT_HTTP_CLIENT_BUILDER));
    }


    public HttpClientBuilder(final Properties properties) {
        this(PropertyResolver.createPropertiesPropertyResolver(
                QBIT_HTTP_CLIENT_BUILDER, properties));
    }

    public static HttpClientBuilder httpClientBuilder() {

        return new HttpClientBuilder();
    }

    public int getFlushInterval() {
        return flushInterval;
    }

    public HttpClientBuilder setFlushInterval(int flushInterval) {
        this.flushInterval = flushInterval;
        return this;
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

    public boolean isSsl() {
        return ssl;
    }

    public HttpClientBuilder setSsl(boolean ssl) {
        this.ssl = ssl;
        return this;
    }

    public boolean isVerifyHost() {
        return verifyHost;
    }

    public HttpClientBuilder setVerifyHost(boolean verifyHost) {
        this.verifyHost = verifyHost;
        return this;
    }

    public boolean isTrustAll() {
        return trustAll;
    }

    public HttpClientBuilder setTrustAll(boolean trustAll) {
        this.trustAll = trustAll;
        return this;
    }

    public int getMaxWebSocketFrameSize() {
        return maxWebSocketFrameSize;
    }

    public HttpClientBuilder setMaxWebSocketFrameSize(int maxWebSocketFrameSize) {
        this.maxWebSocketFrameSize = maxWebSocketFrameSize;
        return this;
    }

    public boolean isTryUseCompression() {
        return tryUseCompression;
    }

    public HttpClientBuilder setTryUseCompression(boolean tryUseCompression) {
        this.tryUseCompression = tryUseCompression;
        return this;
    }

    public String getTrustStorePath() {
        return trustStorePath;
    }

    public HttpClientBuilder setTrustStorePath(String trustStorePath) {
        this.trustStorePath = trustStorePath;
        return this;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public HttpClientBuilder setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
        return this;
    }

    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    public HttpClientBuilder setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
        return this;
    }

    public int getSoLinger() {
        return soLinger;
    }

    public HttpClientBuilder setSoLinger(int soLinger) {
        this.soLinger = soLinger;
        return this;
    }

    public Consumer<Throwable> getErrorHandler() {
        return errorHandler;
    }

    public HttpClientBuilder setErrorHandler(Consumer<Throwable> errorHandler) {
        this.errorHandler = errorHandler;
        return this;
    }

    public HttpClient build() {
        //noinspection UnnecessaryLocalVariable
        @SuppressWarnings("UnnecessaryLocalVariable") final HttpClient httpClient = QBit.factory().createHttpClient(
                this.getHost(),
                this.getPort(),
                this.getTimeOutInMilliseconds(),
                this.getPoolSize(),
                this.isAutoFlush(),
                this.getFlushInterval(),
                this.isKeepAlive(),
                this.isPipeline(),
                this.isSsl(),
                this.isVerifyHost(),
                this.isTrustAll(),
                this.getMaxWebSocketFrameSize(),
                this.isTryUseCompression(),
                this.getTrustStorePath(),
                this.getTrustStorePassword(),
                this.isTcpNoDelay(),
                this.getSoLinger(),
                this.getErrorHandler());

        return httpClient;
    }


    public HttpClient buildAndStart() {
        HttpClient client = build();
        client.start();
        return client;
    }
}
