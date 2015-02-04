package io.advantageous.qbit.events;

/**
 * Created by rhightower on 2/3/15.
 */
public interface EventSubscriber<T> extends EventListener<T> {

    default boolean subscriber() {
        return true;
    }


}
