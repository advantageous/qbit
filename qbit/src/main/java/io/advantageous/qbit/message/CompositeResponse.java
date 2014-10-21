package io.advantageous.qbit.message;


/**
 *
 *
 * @author Rick Hightower
 *
 * @param <M> Message
 * @param <T> Type of message contents
 */
public interface CompositeResponse<M extends Response<T>, T> extends Message<T>, Iterable<M> {


}
