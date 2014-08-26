package org.qbit.transforms;

import org.qbit.queue.OutputQueue;

/**
 * Created by Richard on 7/18/14.
 */
public interface TransformedOutputQueue <V> extends OutputQueue {

    <T> void init(Class<T> finalOutputType, Class<V> offerType,  Transformer<T, V> transformer, OutputQueue<T> wrappedQueue);

}
