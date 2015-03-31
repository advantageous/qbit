package io.advantageous.qbit.events.spi;

import io.advantageous.qbit.service.ServiceFlushable;

public interface EventConnector extends ServiceFlushable{


    /** Forwards the event.
     * @param event   event
     */
    void forwardEvent(EventTransferObject<Object> event);

    default void flush() {

    }
}
