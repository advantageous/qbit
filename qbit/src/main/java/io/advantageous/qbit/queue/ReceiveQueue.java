package io.advantageous.qbit.queue;

import io.advantageous.qbit.Input;


/**
 * Created by Richard on 7/18/14.
 * Represents an receiveQueue queue.
 * This could be a TCP/IP connection.
 * Reading a file from the file system. Etc.
 * A read operation on a database.
 *
 * This is a receive queue.
 *
 * @author Richard Hightower
 *
 *
 *
 */
public interface ReceiveQueue<T> extends Input {

    /** Gets the next item. If the item is null,
     * means the timeout has been reached.
     * @return value from poll
     */
    T pollWait();


    /** Gets the next item. If the item is null the queue currently has no items.
     * @return value from poll
     */
    T poll();

    /** Wait for the next item.
     * @return value from take
     */
    T take();

    /** Read in a batch of items.
     * @param max max number you want from batch
     * @return batch of values
     */
    Iterable<T> readBatch(int max);


    /** Read in a batch of items.
     * @return batch of values
     */
    Iterable<T> readBatch();
}
