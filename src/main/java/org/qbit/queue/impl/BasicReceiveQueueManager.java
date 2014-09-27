package org.qbit.queue.impl;

import org.qbit.queue.ReceiveQueueListener;
import org.qbit.queue.ReceiveQueue;
import org.qbit.queue.ReceiveQueueManager;

import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Created by Richard on 9/8/14.
 */
public class BasicReceiveQueueManager<T> implements ReceiveQueueManager<T> {

    @Override
    public void manageQueue(ReceiveQueue<T> inputQueue, ReceiveQueueListener<T> listener, int batchSize, AtomicBoolean stop) {


        T item = inputQueue.poll(); //Initialize things.

        int count = 0;
        long longCount = 0;

        /* Continues forever or until someone calls stop. */
        while (true) {


            if (item!=null) {
                count++;
            }

            /* Collect a batch of items as long as no item is null. */
            while (item!=null) {

                /* Notify listener that we have an item. */
                listener.receive(item);


                /* If the batch size has hit the max then we need to break. */
                if (count >= batchSize) {
                    listener.limit();
                    break;
                }
                /* Grab the next item from the queue. */
                item = inputQueue.poll();
                count++;

            }

            /* Notify listener that the queue is empty. */
            if (item ==null) {
                listener.empty();
            }
            count = 0;


            item = inputQueue.poll();

            /* See if a yield helps. Try to keep the thread alive. */
            if (item!=null) {
                continue;
            } else {
                Thread.yield();
            }


            /* Get the next item, but wait this time since the queue was empty. */

            item = inputQueue.pollWait();



            if (item==null ) {
                if (longCount % 100 == 0) {
                    if (Thread.currentThread().isInterrupted()) {
                        if (stop.get()) {
                            listener.shutdown();
                            return;
                        }
                    }
                } else if (longCount % 1000 == 0 && stop.get()) {
                    listener.shutdown();
                }
                /* Idle means we yielded and then waited a full wait time, so idle might be a good time to do clean up
                or timed tasks.
                 */
                listener.idle();

            }


            longCount++;

        }


    }
}
