package io.advantageous.qbit.metrics.support;

import io.advantageous.qbit.metrics.StatReplicator;

import java.util.List;

/**
 * Created by rhightower on 1/28/15.
 */
public class ReplicatorHub implements StatReplicator {

    private final List<StatReplicator> list;

    public ReplicatorHub(List<StatReplicator> list) {
        this.list = list;
    }

    @Override
    public void recordCount(String name, int count, long now) {
        for (StatReplicator replicator : list) {
            replicator.recordCount(name, count, now);
        }
    }


}
