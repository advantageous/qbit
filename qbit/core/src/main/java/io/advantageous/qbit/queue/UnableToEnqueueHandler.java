package io.advantageous.qbit.queue;

import java.util.concurrent.BlockingQueue;

public interface UnableToEnqueueHandler {

    default boolean unableToEnqueue(final BlockingQueue<Object> queue, final String queueName, final Object item) {
        return false;
    }

}
