package org.qbit.message;


public interface CompositeResponse<M extends Response<T>, T> extends Message<T>, Iterable<M> {


}
