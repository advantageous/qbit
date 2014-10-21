package io.advantageous.qbit.queue;

import io.advantageous.qbit.Output;

import java.util.Collection;

/**
 * This provides a non-thread safe access to an output queue which allows batching of messages to other threads to
 * minimize thread coordination.
 *
 * Created by Richard on 7/18/14.
 * @author rhightower
 */
public interface SendQueue<T> extends Output {
    void send(T item);
    void sendAndFlush(T item);

    void sendMany(T... item);
    void sendBatch(Collection<T> item);
    void sendBatch(Iterable<T> item);
    boolean shouldBatch();
    void flushSends();


 }
