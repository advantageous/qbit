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
import io.advantageous.qbit.http.client.HttpClient;

/**
 * Client builder is used to createWithWorkers a client programatically.
 * <p>
 * <p>
 * <p>
 * Created by rhightower on 12/3/14.
 */
public class ClientBuilder {


    private String host = "localhost";
    private int port = 8080;
    private boolean autoFlush = true;
    private int pollTime = GlobalConstants.POLL_WAIT;
    private int poolSize = 1;
    private int requestBatchSize = GlobalConstants.BATCH_SIZE;
    private boolean keepAlive = true;
    private boolean pipeline = true;
    private int timeOutInMilliseconds = 3000;
    private int protocolBatchSize = -1;
    private int flushInterval = 500;
    private String uri = "/services";
    private int timeoutSeconds = 30;

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

        Client client = QBit.factory().createClient(uri, httpClient, protocolBatchSize);
        return client;

    }

}
