package io.advantageous.qbit.queue.impl;

import io.advantageous.qbit.queue.SendQueue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.LinkedTransferQueue;

/**
 * This is not thread safe.
 * Create a new for every thread by calling BasicQueue.sendQueue().
 * <p>
 * Created by Richard on 9/8/14.
 * @author rhightower
 */
public class BasicSendQueue<T> implements SendQueue<T> {

    private final LinkedTransferQueue<Object> queue;
    private final Object[] queueLocal;
    private int index;

    private final int batchSize;

    public BasicSendQueue(int batchSize, LinkedTransferQueue<Object> queue) {
        this.batchSize = batchSize;
        this.queue = queue;
        queueLocal = new Object[batchSize];
    }

    public boolean shouldBatch() {
        return !queue.hasWaitingConsumer();
    }

    @Override
    public void send(T item) {
        queueLocal[index] = item;
        index++;
        flushIfOverBatch();
    }

    @Override
    public void sendAndFlush(T item) {
        send(item);
        flushSends();
    }

    @SafeVarargs
    @Override
    public final void sendMany(T... items) {
        flushSends();
        if (!queue.tryTransfer(items)) {
            queue.offer(items);
        }
    }

    @Override
    public void sendBatch(Iterable<T> items) {
        flushSends();
        final Object[] array = objectArray(items);
        if (!queue.tryTransfer(array)) {
            queue.offer(array);
        }
    }

    @Override
    public void sendBatch(Collection<T> items) {
        flushSends();
        final Object[] array = objectArray(items);
        if (!queue.tryTransfer(array)) {
            queue.offer(array);
        }
    }

    private void flushIfOverBatch() {
        if (index >= batchSize) {
            sendLocalQueue();
        }
    }

    @Override
    public void flushSends() {
        if (index > 0) {
            sendLocalQueue();
        }
    }

    private void sendLocalQueue() {
        final Object[] copy = fastObjectArraySlice(queueLocal, 0, index);
        if (!queue.tryTransfer(copy)) {
            queue.offer(copy);
        }
        index = 0;
    }

    static Object[] objectArray(final Iterable iter) {
        if (iter instanceof Collection) {
            final Collection collection = (Collection) iter;
            return collection.toArray(new Object[collection.size()]);
        } else {
            return objectArray(list(iter));
        }
    }

    static <V> List<V> list(final Iterable<V> iterable) {
        final List<V> list = new ArrayList<>();
        for (V o : iterable) {
            list.add(o);
        }
        return list;
    }

    static Object[] fastObjectArraySlice(final Object[] array,
                                         final int start,
                                         final int end) {
        final int newLength = end - start;
        final Object[] newArray = new Object[newLength];
        System.arraycopy(array, start, newArray, 0, newLength);
        return newArray;
    }
}
