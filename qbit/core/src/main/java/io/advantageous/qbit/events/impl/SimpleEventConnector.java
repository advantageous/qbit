package io.advantageous.qbit.events.impl;

import io.advantageous.qbit.events.EventManager;
import io.advantageous.qbit.events.spi.EventConnector;
import io.advantageous.qbit.events.spi.EventTransferObject;

import static io.advantageous.qbit.service.ServiceProxyUtils.flushServiceProxy;

/**
 * A simple connector to bind two event buses.
 *
 * @author Rick Hightower
 */
public class SimpleEventConnector implements EventConnector {

    /**
     * The event bus that we are connecting.
     */
    private final EventManager eventManager;

    public SimpleEventConnector(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    @Override
    public void forwardEvent(EventTransferObject<Object> event) {

        this.eventManager.forwardEvent(event);

    }

    @Override
    public void flush() {

        flushServiceProxy(eventManager);
    }
}
