package io.advantageous.qbit.events.impl;

import io.advantageous.qbit.events.EventConnector;
import io.advantageous.qbit.events.EventManager;
import io.advantageous.qbit.message.Event;

/**
 * @author Rick Hightower
 */
public class SimpleEventConnector implements EventConnector {

    private final EventManager eventManager;

    public SimpleEventConnector(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    @Override
    public void forwardEvent(Event<Object> event) {
        this.eventManager.forwardEvent(event);
    }
}
