package io.advantageous.qbit.queue;

import io.advantageous.qbit.Output;

import java.util.Collection;

/**
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
