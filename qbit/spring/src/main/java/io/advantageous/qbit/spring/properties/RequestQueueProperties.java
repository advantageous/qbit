package io.advantageous.qbit.spring.properties;

import io.advantageous.qbit.GlobalConstants;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration holder for qbit request queue properties.
 *
 * @author geoffc@gmail.com (Geoff Chandler)
 */
@ConfigurationProperties("qbit.queue.request")
public class RequestQueueProperties {
    private int batchSize = GlobalConstants.BATCH_SIZE;
    private int batchCount = GlobalConstants.NUM_BATCHES;
    private int pollWait = GlobalConstants.POLL_WAIT;

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
