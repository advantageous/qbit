package qbit;

/**
 * Created by rhightower on 1/28/15.
 */
public interface StatReplicator {
    void record(String name, int count, long now);
}
