package org.qbit.queue;

import org.qbit.Output;

import java.util.List;

/**
 * Created by Richard on 7/18/14.
 */
public interface SendQueue<T> extends Output {
    boolean offer(T item);
    boolean offerMany(T... item);
    boolean offerBatch(Iterable<T> item);

}
