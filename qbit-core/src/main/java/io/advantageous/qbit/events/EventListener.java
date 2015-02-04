package io.advantageous.qbit.events;

import io.advantageous.qbit.message.Event;

/**
 * Created by rhightower on 2/3/15.
 */
public interface EventListener <T>{

    default boolean subscriber() {
        return true;
    }

    void listen(Event<T> event);

}
