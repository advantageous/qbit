package io.advantageous.qbit.message;


/**
 *
 *
 * @author Rick Hightower
 *
 * @param <M>
 * @param <T>
 */
public interface CompositeResponse<M extends Response<T>, T> extends Message<T>, Iterable<M> {


}
