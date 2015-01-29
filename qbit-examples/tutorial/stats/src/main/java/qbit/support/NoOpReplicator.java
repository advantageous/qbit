package qbit.support;

import qbit.StatReplicator;

/**
 * Created by rhightower on 1/28/15.
 */
public class NoOpReplicator implements StatReplicator {
    @Override
    public final void record(String name, int count, long now) {
    }
}
