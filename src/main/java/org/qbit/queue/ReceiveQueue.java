package org.qbit.queue;

import org.qbit.Input;


/**
 * Created by Richard on 7/18/14.
 * Represents an receiveQueue queue.
 * This could be a TCP/IP connection.
 * Reading a file from the file system. Etc.
 * A read operation on a database.
 * @author Richard Hightower
 *
 */
public interface ReceiveQueue<T> extends Input {

    /** Gets the next item. If the item is null,
     * means the timeout has been reached. */
    T pollWait();


    /** Gets the next item. If the item is null the queue currently has no items. */
    T poll();

    /** Wait for the next item. */
    T take();

    /** Read in a batch of items. */
    Iterable<T> readBatch(int max);


    /** Read in a batch of items. */
    Iterable<T> readBatch();
}
