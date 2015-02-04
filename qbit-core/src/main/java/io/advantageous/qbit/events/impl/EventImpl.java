package io.advantageous.qbit.events.impl;

import io.advantageous.qbit.message.Event;

/**
 * Created by rhightower on 2/3/15.
 */
public class EventImpl<T> implements Event<T> {

    private final T body;
    private final long id;
    private final String topic;

    public EventImpl(T body, long id, String topic) {
        this.body = body;
        this.id = id;
        this.topic = topic;
    }

    @Override
    public long id() {
        return id;
    }

    @Override
    public T body() {
        return body;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public String toString() {
        return "EventImpl{" +
                "body=" + body +
                ", id=" + id +
                '}';
    }

    @Override
    public String topic() {
        return topic;
    }
}
