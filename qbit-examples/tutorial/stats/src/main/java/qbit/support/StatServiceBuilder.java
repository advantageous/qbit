package qbit.support;

import qbit.StatRecorder;
import qbit.StatReplicator;
import qbit.StatService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rhightower on 1/28/15.
 */
public class StatServiceBuilder {

    private StatRecorder recorder = new NoOpRecorder();

    private StatReplicator replicator = new NoOpReplicator();


    private List<StatReplicator> replicators = new ArrayList<>();


    public StatServiceBuilder addReplicator(StatReplicator replicator) {
        replicators.add(replicator);
        return this;
    }

    public StatRecorder getRecorder() {
        return recorder;
    }

    public StatServiceBuilder setRecorder(StatRecorder recorder) {
        this.recorder = recorder;
        return this;
    }

    public StatReplicator getReplicator() {
        return replicator;
    }

    public StatServiceBuilder setReplicator(StatReplicator replicator) {
        this.replicator = replicator;
        return this;
    }

    public StatService build() {

        if (replicators.size()==0) {
            return new StatService(this.getRecorder(), this.getReplicator());
        } else {
            return new StatService(this.getRecorder(), new ReplicatorHub(replicators));
        }
    }
}
