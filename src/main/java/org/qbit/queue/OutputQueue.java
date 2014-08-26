package org.qbit.queue;

import org.qbit.Output;

import java.util.List;

/**
 * Created by Richard on 7/18/14.
 */
public interface OutputQueue <T> extends Output {
    boolean offer(T item);
    List<T> offerMany(T... item);
    List<T> offerBatch(Iterable<T> item);

}
