package io.advantageous.qbit.events.impl;

import io.advantageous.qbit.events.EventRepeater;
import io.advantageous.qbit.message.Event;

import java.util.function.Predicate;

public class EventRepeaterImpl implements EventRepeater{

    private final Predicate<Event<Object>> handleEvent;
    private final EventRepeater remoteRepeater;

    public EventRepeaterImpl(Predicate<Event<Object>> handleEvent, EventRepeater remoteChannel) {
        this.handleEvent = handleEvent;
        this.remoteRepeater = remoteChannel;
    }


    @Override
    public void send(
            final Event<Object> event) {
        if ( handleEvent.test( event ) ) {
            remoteRepeater.send(event);
        }
    }
}
