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


}
