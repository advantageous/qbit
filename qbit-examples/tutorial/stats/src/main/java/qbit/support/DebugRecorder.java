package qbit.support;

import qbit.StatRecorder;

import java.util.List;

import static org.boon.Boon.puts;

/**
 * Created by rhightower on 1/28/15.
 */
public class DebugRecorder implements StatRecorder {
    public volatile int count;
    public boolean out=false;

    public DebugRecorder(boolean out) {
        this.out = out;
    }

    public DebugRecorder() {
    }


    @Override
    public void record(List<MinuteStat> records) {
        count += records.size();
        if(out)puts("DEBUG RECORDER", records);
    }
}
