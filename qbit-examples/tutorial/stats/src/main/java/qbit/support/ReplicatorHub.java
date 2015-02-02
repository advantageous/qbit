package qbit.support;

import qbit.StatReplicator;

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
    public void record(String name, int count, long now) {
        for (StatReplicator replicator : list) {
            replicator.record(name, count, now);
        }
    }


}
