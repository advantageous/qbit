package qbit;

import qbit.support.MinuteStat;

import java.util.List;

/**
 * Created by rhightower on 1/28/15.
 */
public interface StatRecorder {

    void record(List<MinuteStat> records);
}
