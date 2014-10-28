package io.advantageous.qbit.message;

/**
 * @author rhightower
 * @param <M> message
 * @param <T> type
 */
public interface CompositeMessage<M extends Message<T>, T> extends Message<T>, Iterable<M> {


}
