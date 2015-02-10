package io.advantageous.qbit.metrics.support;


import io.advantageous.qbit.metrics.StatRecorder;

import java.util.List;

/**
 * Created by rhightower on 1/28/15.
 */
public class NoOpRecorder implements StatRecorder {

    @Override
    public final void record(List<MinuteStat> record) {
    }
}
