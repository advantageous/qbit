package io.advantageous.qbit.events.impl;

import io.advantageous.qbit.events.spi.EventConnector;
import io.advantageous.qbit.events.spi.EventTransferObject;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 */
public class EventConnectorHub implements EventConnector, Iterable<EventConnector> {

    private final List<EventConnector> eventConnectors;

    public EventConnectorHub(final List<EventConnector> eventConnectors) {

        this.eventConnectors = new CopyOnWriteArrayList<>(eventConnectors);
    }

    public EventConnectorHub() {

        this.eventConnectors = new CopyOnWriteArrayList<>();
    }

    public void add(EventConnector eventConnector) {
        this.eventConnectors.add(eventConnector);
    }

    public void addAll(EventConnector... eventConnectors) {
        for (EventConnector eventConnector : eventConnectors) {
            this.eventConnectors.add(eventConnector);
        }
    }


    public void remove(EventConnector eventConnector) {
        this.eventConnectors.remove(eventConnector);
    }


    @Override
    public void forwardEvent(final EventTransferObject<Object> event) {
        for (int index=0; index < eventConnectors.size(); index++) {
            eventConnectors.get(index).forwardEvent(event);
        }
    }

    @Override
    public void flush() {
        for (int index=0; index < eventConnectors.size(); index++) {
            eventConnectors.get(index).flush();
        }
    }

    @Override
    public Iterator<EventConnector> iterator() {
        return this.eventConnectors.listIterator();
    }

    public ListIterator<EventConnector> listIterator() {
        return this.eventConnectors.listIterator();
    }

}
