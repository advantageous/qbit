package io.advantageous.qbit.message;

/**
 * Created by Richard on 7/22/14.
 */
public interface Event<T> extends Message<T> {
    String topic();
}
