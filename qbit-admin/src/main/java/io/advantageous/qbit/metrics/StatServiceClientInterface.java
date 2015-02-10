package io.advantageous.qbit.metrics;

import io.advantageous.qbit.client.ClientProxy;
import io.advantageous.qbit.service.Callback;

/**
 * Created by rhightower on 1/28/15.
 */

public interface StatServiceClientInterface extends ClientProxy {
    void recordCount(String name, int count);


    void currentMinuteCount(Callback<Integer> callback, String name);
    void currentSecondCount(Callback<Integer> callback, String name);
    void lastSecondCount(Callback<Integer> callback, String name);


    void recordCountWithTime(String name, int count, long timestamp);
    void recordAllCounts(long timestamp, String[] names, int[] counts);
    void recordAllCountsWithTimes(String[] names, int[] counts, long[] times);
}
