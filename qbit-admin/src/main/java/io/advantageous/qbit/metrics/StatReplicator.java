package io.advantageous.qbit.metrics;

/**
 * Created by rhightower on 1/28/15.
 */
public interface StatReplicator {
    void recordCount(String name, int count, long now);
}
