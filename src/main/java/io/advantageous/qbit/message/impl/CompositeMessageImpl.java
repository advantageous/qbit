package io.advantageous.qbit.message.impl;

import io.advantageous.qbit.Timer;
import io.advantageous.qbit.message.CompositeMessage;
import io.advantageous.qbit.message.Message;

import java.util.Iterator;
import java.util.List;

/**
 * This is a composite message passed to a service.
 * <p>
 * Created by Richard on 9/8/14.
 */
public class CompositeMessageImpl<M extends Message<T>, T> implements CompositeMessage<M, T> {

    private final long id;
    private final long timestamp;
    private List<M> messages;

    private static volatile long idSequence;

    public CompositeMessageImpl(List<M> messages) {
        this.messages = messages;
        this.id = idSequence++;
        this.timestamp = Timer.timer().time();
    }

    @Override
    public Iterator<M> iterator() {
        return messages.iterator();
    }

    @Override
    public long id() {
        return id;
    }


    @Override
    public T body() {
        throw new UnsupportedOperationException("This is a composite message");
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
