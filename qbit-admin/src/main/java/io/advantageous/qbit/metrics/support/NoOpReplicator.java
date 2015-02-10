package io.advantageous.qbit.metrics.support;


import io.advantageous.qbit.metrics.StatReplicator;

/**
 * Created by rhightower on 1/28/15.
 */
public class NoOpReplicator implements StatReplicator {
    @Override
    public final void recordCount(String name, int count, long now) {
    }
}
