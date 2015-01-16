package io.advantageous.qbit.message.impl;

import io.advantageous.qbit.message.CompositeResponse;
import io.advantageous.qbit.message.Response;

import java.util.Iterator;
import java.util.List;

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
