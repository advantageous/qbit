package io.advantageous.qbit.queue;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Richard on 9/8/14.
 * This abstracts out the way we handle queue operations so we can have various strategies for different
 * types of queues. We could for example have a queue manager that was optimized for CPU intensive tasks
 * or one that was optimized for I/O.
 *
 * @author rhightower
 */
public interface ReceiveQueueManager <T> {

    void manageQueue(ReceiveQueue<T> queue, ReceiveQueueListener<T> listener, int batchSize, AtomicBoolean stop);
}
