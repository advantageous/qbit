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
 * @author rhightower
 *         on 2/14/15.
 */
public class HttpServerOptions implements Cloneable {

    protected String host;
    protected int port = 8080;
    protected boolean manageQueues = false;
    protected int maxRequestBatches = 1_000_000;
    protected boolean pipeline = true;
    protected int pollTime = 100;
    protected int requestBatchSize = 10;
    protected int flushInterval = 100;
    protected int workers = -1;
    protected boolean tcpNoDelay = true;
    protected int soLinger = 0;
    protected boolean usePooledBuffers = true;
    protected int acceptBackLog = 1_000_000;
    protected boolean keepAlive = true;
    protected int maxWebSocketFrameSize = 100_000_000;
    protected boolean compressionSupport = false;
    protected boolean reuseAddress = true;
    protected int idleTimeout = 30_000;


    public HttpServerOptions() {

    }


    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /*
     * Getter Methods
     */
    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public boolean isManageQueues() {
        return manageQueues;
    }

    public int getMaxRequestBatches() {
        return maxRequestBatches;
    }

    public boolean isPipeline() {
        return pipeline;
    }

    public int getPollTime() {
        return pollTime;
    }

    public int getRequestBatchSize() {
        return requestBatchSize;
    }

    public int getFlushInterval() {
        return flushInterval;
    }

    public int getWorkers() {
        return workers;
    }

    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    public int getSoLinger() {
        return soLinger;
    }

    public boolean isUsePooledBuffers() {
        return usePooledBuffers;
    }

    public int getAcceptBackLog() {
        return acceptBackLog;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public int getMaxWebSocketFrameSize() {
        return maxWebSocketFrameSize;
    }

    public boolean isCompressionSupport() {
        return compressionSupport;
    }

    public boolean isReuseAddress() {
        return reuseAddress;
    }


    public int getIdleTimeout() {
        return idleTimeout;
    }

    @Override
    public String toString() {
        return "HttpServerOptions{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", manageQueues=" + manageQueues +
                ", maxRequestBatches=" + maxRequestBatches +
                ", pipeline=" + pipeline +
                ", pollTime=" + pollTime +
                ", requestBatchSize=" + requestBatchSize +
                ", flushInterval=" + flushInterval +
                ", workers=" + workers +
                ", tcpNoDelay=" + tcpNoDelay +
                ", soLinger=" + soLinger +
                ", usePooledBuffers=" + usePooledBuffers +
                ", acceptBackLog=" + acceptBackLog +
                ", keepAlive=" + keepAlive +
                ", maxWebSocketFrameSize=" + maxWebSocketFrameSize +
                ", compressionSupport=" + compressionSupport +
                ", reuseAddress=" + reuseAddress +
                '}';
    }
}
