package io.advantageous.qbit.queue;

import java.util.concurrent.BlockingQueue;

public class DefaultUnableToEnqueueHandler implements UnableToEnqueueHandler {


    public boolean unableToEnqueue(BlockingQueue<Object> queue, String name) {

        throw new QueueException("QUEUE FULL: Unable to send messages to queue " + name);

    }

}
