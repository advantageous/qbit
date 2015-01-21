package io.advantageous.qbit.queue.impl;

import io.advantageous.qbit.queue.ReceiveQueue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;

import static org.boon.Boon.puts;


/**
 * This is not thread safe.
 * You use BasicQueue to create this in the one thread that you are going to use it.
 * Created by Richard on 9/8/14.
 * @author rhightower
*/
class BasicReceiveQueue<T> implements ReceiveQueue<T> {

    private final long waitTime;
    private final TimeUnit timeUnit;
    private final int batchSize;
    private Object[] lastQueue = null;
    private int lastQueueIndex;
    private final BlockingQueue<Object> queue;

    public BasicReceiveQueue(BlockingQueue<Object> queue, long waitTime, TimeUnit timeUnit, int batchSize) {
        this.queue = queue;
        this.waitTime = waitTime;
        this.timeUnit = timeUnit;
        this.batchSize = batchSize;
    }

    @Override
    public T pollWait() {

        if (lastQueue!=null) {

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
        T item = (T)lastQueue[lastQueueIndex];
        lastQueueIndex++;

        if (lastQueueIndex == lastQueue.length) {
            lastQueueIndex = 0;
            lastQueue = null;
        }
        return item;
    }


    @Override
    public T poll() {

        if (lastQueue!=null) {

            return getItemFromLocalQueue();
        }

        Object o =   queue.poll();
        return extractItem(o);

    }

    @Override
    public T take() {

        if (lastQueue!=null) {

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

        if (o instanceof Object[]) {

            lastQueue = (Object[]) o;
            //uts("batch size", lastQueue.length);
            return getItemFromLocalQueue();
        } else {
            return (T)o;
        }
    }

    @Override
    public Iterable<T> readBatch(int max) {

        T item = this.poll();
        if (item==null) {
            return Collections.emptyList();
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
