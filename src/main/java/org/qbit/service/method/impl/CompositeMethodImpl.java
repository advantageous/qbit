package org.qbit.service.method.impl;

import org.qbit.message.CompositeMethod;
import org.qbit.message.MethodCall;
import org.qbit.message.impl.CompositeMessageImpl;

import java.util.Iterator;

import java.util.List;
import java.util.Map;

/**
 * Created by Richard on 9/8/14.
 */
public class CompositeMethodImpl <M extends MethodCall<T>, T> implements CompositeMethod<M, T> {

    private CompositeMessageImpl<M, T> methods;



    public CompositeMethodImpl(List<M> messages) {

        methods = new CompositeMessageImpl<>(messages);

    }

    @Override
    public Iterator<M> iterator() {
        return methods.iterator();
    }

    @Override
    public long id() {
        return methods.id();
    }


    @Override
    public T body() {
        return methods.body();
    }

    @Override
    public boolean isSingleton() {
        return methods.isSingleton();
    }
}
