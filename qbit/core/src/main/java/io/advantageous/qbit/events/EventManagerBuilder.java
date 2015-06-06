package io.advantageous.qbit.events;

import io.advantageous.qbit.QBit;
import io.advantageous.qbit.events.impl.ConditionalEventConnector;
import io.advantageous.qbit.events.spi.EventConnector;
import io.advantageous.qbit.message.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class EventManagerBuilder {

    private EventConnector eventConnector;
    private List<Predicate<Event<Object>>> eventConnectorPredicates = new ArrayList<>();

    public static EventManagerBuilder eventManagerBuilder() {
        return new EventManagerBuilder();
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

        if (eventConnectorPredicates == null) {
            eventConnectorPredicates = new ArrayList<>();
        }
        eventConnectorPredicates.add(eventConnectorPredicate);
        return this;
    }


    public EventManager build() {
        return build("no_name");
    }

    public EventManager build(final String name) {
        if (eventConnector == null) {
            return QBit.factory().createEventManager(name);

        } else {

            if (eventConnectorPredicates.size() == 0) {
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
