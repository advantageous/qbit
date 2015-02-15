package io.advantageous.qbit.http.config;

/**
 * Created by rhightower on 2/14/15.
 */
public class HttpServerOptions implements Cloneable{

    protected String host;
    protected int port = 8080;
    protected boolean manageQueues = true;
    protected int maxRequestBatches = 1_000_000;
    protected boolean pipeline = true;
    protected int pollTime = 100;
    protected int requestBatchSize = 10;
    protected int flushInterval = 100;
    protected int workers = -1;

    public HttpServerOptions() {

    }


    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public int getWorkers() {
        return workers;
    }

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
                '}';
    }
}
