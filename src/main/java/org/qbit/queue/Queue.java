package org.qbit.queue;

/**
 * Created by Richard on 8/4/14.
 */
public interface Queue <T> {
    ReceiveQueue<T> receive();
    SendQueue<T> send();

    void startListener(ReceiveQueueListener<T> listener);

    void stop();
}
