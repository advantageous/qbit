package org.qbit.message;

public interface CompositeMessage<M extends Message<T>, T> extends Message<T>, Iterable<M> {


}
