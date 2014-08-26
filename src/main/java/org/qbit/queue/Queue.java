package org.qbit.queue;

/**
 * Created by Richard on 8/4/14.
 */
public interface Queue <T> {
    InputQueue<T> input();
    OutputQueue<T> output();

    void startListener(InputQueueListener<T> listener);

    void stop();
}
