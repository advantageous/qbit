package io.advantageous.qbit.events;

import io.advantageous.qbit.message.Event;

public interface EventConnector {


    /**
     * @param event   event
     */
    void forwardEvent(Event<Object> event);

}
