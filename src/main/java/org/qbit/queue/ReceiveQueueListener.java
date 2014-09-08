package org.qbit.queue;

import org.qbit.Input;

/**
 * Created by Richard on 7/18/14.
 * Simplifies queue handler loop code by abstracting queue operations.
 * @author Richard Hightower
 */
public interface ReceiveQueueListener<T> extends Input {

    /** Notifies a queue listener that an item has been recieved */
    void receive(T item);

    /** Notifies the queue listener that currently the queue is empty.
     * This is good for batch operations. This could mean the queue is empty or we reached our max batch size limit.
     *
     * */
    void empty();


    /** Notifies the queue listener that we processed up to batch size.
     * This is good for batch operations. This could mean the queue is empty or we reached our max batch size limit.
     *
     * */
    void limit();

    /** Notifies the queue listener that currently the queue is closed for business. */
    void shutdown();

    /** This means we did not find an item. We waited for an item as well and there was still not an item in the queue
     * This would be a good time to do some clean up.
     */
    void idle();
}
