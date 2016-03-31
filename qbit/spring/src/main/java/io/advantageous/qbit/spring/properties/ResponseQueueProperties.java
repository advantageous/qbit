package io.advantageous.qbit.spring.properties;

import io.advantageous.boon.core.Sys;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration holder for qbit response queue properties.
 *
 * @author geoffc@gmail.com (Geoff Chandler)
 */
@ConfigurationProperties("qbit.queue.response")
public class ResponseQueueProperties {
    private int batchSize = Sys.sysProp(ResponseQueueProperties.class.getName() + ".batchSize", 1_000);
    private int batchCount = Sys.sysProp(ResponseQueueProperties.class.getName() + ".batchCount", 100_000);
    private int pollWait = Sys.sysProp(ResponseQueueProperties.class.getName() + ".pollWait", 15);

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public int getBatchCount() {
        return batchCount;
    }

    public void setBatchCount(int batchCount) {
        this.batchCount = batchCount;
    }

    public int getPollWait() {
        return pollWait;
    }

    public void setPollWait(int pollWait) {
        this.pollWait = pollWait;
    }
}
