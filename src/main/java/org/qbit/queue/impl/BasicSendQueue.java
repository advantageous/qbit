package org.qbit.queue.impl;

import org.boon.primitive.Arry;
import org.qbit.queue.SendQueue;

import java.util.Collection;
import java.util.concurrent.LinkedTransferQueue;

/**
 * This is not thread safe.
 * Create a new for every thread by calling BasicQueue.sendQueue().
 * Created by Richard on 9/8/14.
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


    @Override
    public void sendMany(T... items) {


        flushSends();


        if (!queue.tryTransfer(items)) {
            queue.offer(items);
        }


    }

    @Override
    public void sendBatch(Iterable<T> items) {

        flushSends();


        final Object[] array = Arry.objectArray(items);

        if (!queue.tryTransfer(array)) {
            queue.offer(array);
        }
    }

    @Override
    public void sendBatch(Collection<T> items) {

        flushSends();


        final Object[] array = Arry.objectArray(items);

        if (!queue.tryTransfer(array)) {
            queue.offer(array);
        }
    }



    private void flushIfOverBatch() {
        if (index >= batchSize) {
            sendLocalQueue();
        }

//        if (index > (batchSize/10) ) {
//            if (shouldBatch()) {
//                sendLocalQueue();
//            }
//        }

    }

    @Override
    public void flushSends() {

        if (index > 0) {
            sendLocalQueue();
        }

    }

    private void sendLocalQueue() {


        final Object[] copy = Arry.fastObjectArraySlice(queueLocal, 0, index);

        if (!queue.tryTransfer(copy)) {
            queue.offer(copy);
        }
        index=0;

    }

}
