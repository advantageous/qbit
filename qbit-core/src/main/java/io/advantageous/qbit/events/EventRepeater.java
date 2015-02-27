package io.advantageous.qbit.events;

import io.advantageous.qbit.message.Event;

public interface EventRepeater {


    /**
     * @param event   event
     */
    void send(Event<Object> event);

}
