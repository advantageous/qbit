package org.qbit.queue;

/**
 * Created by Richard on 8/4/14.
 */
public interface Queue <T> {
    ReceiveQueue<T> receiveQueue();
    SendQueue<T> sendQueue();

    void startListener(ReceiveQueueListener<T> listener);

    void stop();
}
