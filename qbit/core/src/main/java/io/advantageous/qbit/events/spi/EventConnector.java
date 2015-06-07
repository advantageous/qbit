package io.advantageous.qbit.events.spi;

import io.advantageous.qbit.service.ServiceFlushable;

/**
 * An event connector knows how to connect events from an event bus to some place else.
 * This allows you to connect the event bus to JMS, Kafka, RabbitMQ, etc.
 */
public interface EventConnector extends ServiceFlushable {


    /**
     * Forwards the event.
     *
     * @param event event
     */
    void forwardEvent(EventTransferObject<Object> event);

    default void flush() {

    }
}
