package io.advantageous.qbit.message;

/**
 * @author rhightower
 * @param <M>
 * @param <T>
 */
public interface CompositeMessage<M extends Message<T>, T> extends Message<T>, Iterable<M> {


}
