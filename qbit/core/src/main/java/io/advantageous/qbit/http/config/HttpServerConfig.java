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

package io.advantageous.qbit.http.config;

/**
 * Created by rhightower on 2/14/15.
 */
public class HttpServerConfig extends HttpServerOptions {


    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setManageQueues(boolean manageQueues) {
        this.manageQueues = manageQueues;
    }

    public void setMaxRequestBatches(int maxRequestBatches) {
        this.maxRequestBatches = maxRequestBatches;
    }

    public void setPipeline(boolean pipeline) {
        this.pipeline = pipeline;
    }

    public void setPollTime(int pollTime) {
        this.pollTime = pollTime;
    }

    public void setRequestBatchSize(int requestBatchSize) {
        this.requestBatchSize = requestBatchSize;
    }

    public void setFlushInterval(int flushInterval) {
        this.flushInterval = flushInterval;
    }

    public void setWorkers(int workers) {
        this.workers = workers;
    }

    public void setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    public void setSoLinger(int soLinger) {
        this.soLinger = soLinger;
    }

    public void setUsePooledBuffers(boolean usePooledBuffers) {
        this.usePooledBuffers = usePooledBuffers;
    }

    public void setAcceptBackLog(int acceptBackLog) {
        this.acceptBackLog = acceptBackLog;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public void setMaxWebSocketFrameSize(int maxWebSocketFrameSize) {
        this.maxWebSocketFrameSize = maxWebSocketFrameSize;
    }

    public void setCompressionSupport(boolean compressionSupport) {
        this.compressionSupport = compressionSupport;
    }

    public void setReuseAddress(boolean reuseAddress) {
        this.reuseAddress = reuseAddress;
    }

    public void setIdleTimeout(int idleTimeout) {
        this.idleTimeout = idleTimeout;
    }


}
