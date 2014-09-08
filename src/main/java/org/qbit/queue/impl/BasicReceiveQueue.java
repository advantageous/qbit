package org.qbit.queue.impl;

import org.qbit.queue.ReceiveQueue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;

/**
* Created by Richard on 9/8/14.
*/
class BasicReceiveQueue<T> implements ReceiveQueue<T> {

    private final long waitTime;
    private final TimeUnit timeUnit;
    private final int batchSize;
    private ThreadLocal<java.util.Queue> lastQueue = new ThreadLocal<>();
    private final LinkedTransferQueue<Object> queue;

    public BasicReceiveQueue(LinkedTransferQueue<Object> queue, long waitTime, TimeUnit timeUnit, int batchSize) {
        this.queue = queue;
        this.waitTime = waitTime;
        this.timeUnit = timeUnit;
        this.batchSize = batchSize;
    }

    @Override
    public T pollWait() {

        if (lastQueue.get()!=null) {

            return getItemFromLocalQueue();
        }

        try {

            Object o =  queue.poll(waitTime, timeUnit);
            return extractItem(o);
        } catch (InterruptedException e) {
            return null;
        }
    }

    private T getItemFromLocalQueue() {
        Object item = lastQueue.get().poll();
        if (lastQueue.get().size()==0) {
            lastQueue.set(null);
        }
        return (T) item;
    }


    @Override
    public T poll() {

        if (lastQueue.get()!=null) {

            return getItemFromLocalQueue();
        }

        Object o =   queue.poll();
        return extractItem(o);

    }

    @Override
    public T take() {

        if (lastQueue.get()!=null) {

            return getItemFromLocalQueue();
        }

        try {
            Object o =  queue.take();
            return extractItem(o);

        } catch (InterruptedException e) {
            Thread.interrupted();
            return null;
        }
    }

    private T extractItem(Object o) {
        if (o instanceof java.util.Queue) {
            lastQueue.set((java.util.Queue) o);
            return (T) lastQueue.get().poll();
        } else {
            return (T)o;
        }
    }

    @Override
    public Iterable<T> readBatch(int max) {

        T item = this.poll();
        if (item==null) {
            return Collections.EMPTY_LIST;
        } else {
            List<T> batch = new ArrayList<>();
            batch.add(item);
            while ((item = this.poll()) != null) {
                batch.add(item);
            }
            return batch;
        }
    }

    @Override
    public Iterable<T> readBatch() {
        return readBatch(batchSize);
    }
}
