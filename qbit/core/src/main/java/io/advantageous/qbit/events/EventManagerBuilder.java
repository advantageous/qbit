package io.advantageous.qbit.events;

import io.advantageous.qbit.QBit;
import io.advantageous.qbit.events.impl.ConditionalEventConnector;
import io.advantageous.qbit.events.spi.EventConnector;
import io.advantageous.qbit.message.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Builds an event manager and allows you to specify connectors,
 * and connector predicates for said connector.
 */
public class EventManagerBuilder {

    private EventConnector eventConnector;
    private List<Predicate<Event<Object>>> eventConnectorPredicates = new ArrayList<>();
    private String name;


    public static EventManagerBuilder eventManagerBuilder() {
        return new EventManagerBuilder();
    }


    public String getName() {
        return name;
    }

    public EventManagerBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public EventConnector getEventConnector() {
        return eventConnector;
    }

    public List<Predicate<Event<Object>>> getEventConnectorPredicates() {
        if (eventConnectorPredicates==null) {
            eventConnectorPredicates = new ArrayList<>();
        }
        return eventConnectorPredicates;
    }

    public EventManagerBuilder setEventConnector(EventConnector eventConnector) {
        this.eventConnector = eventConnector;
        return this;
    }

    public EventManagerBuilder setEventConnectorPredicates(List<Predicate<Event<Object>>> eventConnectorPredicates) {
        this.eventConnectorPredicates = eventConnectorPredicates;
        return this;
    }

    public EventManagerBuilder addEventConnectorPredicate(Predicate<Event<Object>> eventConnectorPredicate) {

        getEventConnectorPredicates().add(eventConnectorPredicate);
        return this;
    }


    public EventManager build() {
        return build(getName());
    }

    public EventManager build(final String name) {
        if ( getEventConnector() == null) {
            return QBit.factory().createEventManager(name);

        } else {

            if (getEventConnectorPredicates().size() == 0) {
                return QBit.factory().createEventManagerWithConnector(name, eventConnector);
            } else {

                Predicate<Event<Object>> mainPredicate = eventConnectorPredicates.get(0);

                for (int index = 1; index < eventConnectorPredicates.size(); index++) {
                    mainPredicate = mainPredicate.and(eventConnectorPredicates.get(index));
                }
                return QBit.factory().createEventManagerWithConnector(name,
                        new ConditionalEventConnector(mainPredicate, eventConnector));
            }

        }
    }

}
