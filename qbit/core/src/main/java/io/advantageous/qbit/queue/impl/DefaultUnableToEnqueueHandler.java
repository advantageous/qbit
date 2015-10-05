package io.advantageous.qbit.queue.impl;

import io.advantageous.qbit.queue.QueueException;
import io.advantageous.qbit.queue.UnableToEnqueueHandler;

import java.util.concurrent.BlockingQueue;

public class DefaultUnableToEnqueueHandler implements UnableToEnqueueHandler {


    public boolean unableToEnqueue(BlockingQueue<Object> queue, String name, Object item) {

        throw new QueueException("QUEUE FULL: Unable to send messages to queue " + name);

    }

}
