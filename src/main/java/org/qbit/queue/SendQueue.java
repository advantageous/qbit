package org.qbit.queue;

import org.qbit.Output;

import java.util.Collection;
import java.util.List;

/**
 * Created by Richard on 7/18/14.
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
