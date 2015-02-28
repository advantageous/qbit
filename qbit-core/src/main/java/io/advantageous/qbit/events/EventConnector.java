package io.advantageous.qbit.events;

import io.advantageous.qbit.message.Event;
import io.advantageous.qbit.service.ServiceFlushable;

public interface EventConnector extends ServiceFlushable{


    /**
     * @param event   event
     */
    void forwardEvent(Event<Object> event);

    default void flush() {

    }



}
