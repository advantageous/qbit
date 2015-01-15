package io.advantageous.qbit.queue;

import io.advantageous.qbit.Input;

/**
 * Created by Richard on 7/18/14.
 * Simplifies queue handler loop code by abstracting queue operations.
 *
 * The nitty gritty of polling the queue which can vary from one implementation to another is abstracted
 * These are all callback method to notify you when the queue has an item, when the queue is empty, etc.
 * 
 * @author Richard Hightower
 */
public interface ReceiveQueueListener<T> extends Input {

    /** Notifies a queue listener that an item has been received
     * @param item item
     */
    void receive(T item);

    /** Notifies the queue listener that currently the queue is empty.
     * This is good for batch operations. This could mean the queue is empty or we reached our max batch size limit.
     *
     * */
    default void empty() {};


    /** Notifies the queue listener that we processed up to batch size.
     * This is good for batch operations. This could mean the queue is empty or we reached our max batch size limit.
     *
     * */
    default void limit() {}

    /** Notifies the queue listener that currently the queue is closed for business. */
    default void shutdown(){};

    /** This means we did not find an item. We waited for an item as well and there was still not an item in the queue
     * This would be a good time to do some clean up.
     */
    default void idle(){}
}
