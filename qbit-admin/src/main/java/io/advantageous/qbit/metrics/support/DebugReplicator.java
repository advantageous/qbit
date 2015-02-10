package io.advantageous.qbit.metrics.support;


import io.advantageous.qbit.metrics.StatReplicator;

import static org.boon.Boon.puts;

/**
 * Created by rhightower on 1/28/15.
 */
public class DebugReplicator implements StatReplicator {

    public volatile int count;

    public boolean out=false;

    public DebugReplicator(boolean out) {
        this.out = out;
    }

    public DebugReplicator() {
    }


    @Override
    public void recordCount(String name, int count, long now) {
        this.count += count;
        if (out) puts("DEBUG REPLICATOR", name, count, now);
    }
}
