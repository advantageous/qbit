package io.advantageous.qbit.queue.impl;

import io.advantageous.qbit.queue.QueueException;
import io.advantageous.qbit.queue.UnableToEnqueueHandler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class AddTimeoutUnableToEnqueueHandler implements UnableToEnqueueHandler {

    private final int timeout;
    private final TimeUnit timeUnit;

    public AddTimeoutUnableToEnqueueHandler(int timeout, TimeUnit timeUnit) {
        this.timeout = timeout;
        this.timeUnit = timeUnit;
    }


    public boolean unableToEnqueue(BlockingQueue<Object> queue, String name, Object item) {
        try {
            if (!queue.offer(item, timeout, timeUnit)) {
                throw new QueueException("QUEUE FULL: After timeout Unable to send messages to queue " + this);
            }
            return true;
        } catch (InterruptedException e) {
            throw new QueueException("QUEUE FULL: Unable to send messages to queue " + name);
        }

    }

    @Override
    public String toString() {
        return "AddTimeoutUnableToEnqueueHandler{" +
                "timeout=" + timeout +
                ", timeUnit=" + timeUnit +
                '}';
    }
}
