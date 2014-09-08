package org.qbit.service.method.impl;

import org.qbit.message.CompositeResponse;
import org.qbit.message.Response;
import org.qbit.message.impl.CompositeMessageImpl;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Richard on 9/8/14.
 */
public class CompositeResponseImpl <M extends Response<T>, T> implements CompositeResponse<M, T> {

    private CompositeMessageImpl<M, T> responses;



    public CompositeResponseImpl(List<M> responses) {

        this.responses = new CompositeMessageImpl<>(responses);

    }

    @Override
    public Iterator<M> iterator() {
        return responses.iterator();
    }

    @Override
    public long id() {
        return responses.id();
    }


    @Override
    public T body() {
        return responses.body();
    }

    @Override
    public boolean isSingleton() {
        return responses.isSingleton();
    }
}
