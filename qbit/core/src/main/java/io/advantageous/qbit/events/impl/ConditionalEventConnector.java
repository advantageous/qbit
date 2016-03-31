package io.advantageous.qbit.events.impl;

import io.advantageous.qbit.events.spi.EventConnector;
import io.advantageous.qbit.events.spi.EventTransferObject;
import io.advantageous.qbit.message.Event;

import java.util.function.Predicate;

/**
 * Allows you to modify which events get sent to an event connector.
 * The predicate can inspect the event and decide to send or
 * not to send an event to a given event connector.
 * <p>
 * You would use this for example if you wanted some events replicated to Kafka, but not all events.
 */
public class ConditionalEventConnector implements EventConnector {

    private final Predicate<Event<Object>> handleEvent;
    private final EventConnector eventConnector;

    public ConditionalEventConnector(Predicate<Event<Object>> handleEvent, EventConnector remoteChannel) {
        this.handleEvent = handleEvent;
        this.eventConnector = remoteChannel;
    }


    @Override
    public void forwardEvent(
            final EventTransferObject<Object> event) {
        if (handleEvent.test(event)) {
            eventConnector.forwardEvent(event);
        }
    }
}
