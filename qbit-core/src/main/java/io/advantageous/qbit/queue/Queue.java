package io.advantageous.qbit.queue;

/**
 * Represents a queue manager.
 * Queues are split up into receivers views and send views to facilitate batching.
 * Created by Richard on 8/4/14.
 * @author rhightower
 */
public interface Queue <T> {

    /**
     * This returns a thread safe receive queue. Pulling an item off of the queue makes it unavailable to other thread.
     * @return receive queue
     */
    ReceiveQueue<T> receiveQueue();

    /**
     * This returns a NON-thread safe send queue.
     * It is not thread safe so that you can batch sends to minimize thread hand-off
     * and to maximize IO throughput. Each call to this method returns a send queue
     * that can only be access from one thread.
     * You get MT behavior by having a SendQueue per thread.
     * @return send queue
     */
    SendQueue<T> sendQueue();

    /**
     * This starts up a listener which will listen to items on the
     * receive queue. It will notify when the queue is empty, when the queue is idle, when the queue is shutdown, etc.
     *
     * An idle queue is an indication that it is a good time to do periodic cleanup, etc.
     * @param listener listener
     */
    void startListener(ReceiveQueueListener<T> listener);

    /**
     * Stop the listener.
     */
    void stop();
}
