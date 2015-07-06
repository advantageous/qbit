package io.advantageous.qbit.queue;

import java.util.concurrent.BlockingQueue;

public interface UnableToEnqueueHandler {

    default boolean unableToEnqueue(BlockingQueue<Object> queue, String queueName) {
        return false;
    }

}
