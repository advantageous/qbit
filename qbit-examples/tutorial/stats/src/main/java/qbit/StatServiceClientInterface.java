package qbit;

import io.advantageous.qbit.service.Callback;

/**
 * Created by rhightower on 1/28/15.
 */
public interface StatServiceClientInterface {
    void record(String name, int count);

    void currentMinuteCount(Callback<Integer> callback, String name);

    void currentSecondCount(Callback<Integer> callback, String name);

    void lastSecondCount(Callback<Integer> callback, String name);

    void recordWithTime(String name, int count, long now);

    void recordAll(String[] names, int[] counts);

    void recordAllWithTimes(String[] names, int[] counts, long[] times);
}
