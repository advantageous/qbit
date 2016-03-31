package io.advantageous.qbit.events;

import io.advantageous.qbit.Factory;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.events.impl.ConditionalEventConnector;
import io.advantageous.qbit.events.spi.EventConnector;
import io.advantageous.qbit.message.Event;
import io.advantageous.qbit.service.stats.StatsCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Builds an event manager and allows you to specify connectors,
 * and connector predicates for said connector.
 */
public class EventManagerBuilder {

    public static EventConnector DEFAULT_NO_EVENT_CONNECTOR = event -> {
    };
    public static StatsCollector DEFAULT_NO_STATS_COLLECTOR = new StatsCollector() {
    };
    private final Logger logger = LoggerFactory.getLogger(EventManagerBuilder.class);
    private EventConnector eventConnector;
    private List<Predicate<Event<Object>>> eventConnectorPredicates = new ArrayList<>();
    private String name;
    private StatsCollector statsCollector;
    private Factory factory;


    public static EventManagerBuilder eventManagerBuilder() {
        return new EventManagerBuilder();
    }


    public Factory getFactory() {
        if (factory == null) {
            factory = QBit.factory();
        }
        return factory;
    }

    public EventManagerBuilder setFactory(Factory factory) {
        this.factory = factory;
        return this;
    }

    public StatsCollector getStatsCollector() {
        if (statsCollector == null) {
            logger.debug("No stats collector registered with event manager, using default NO OP stats collector");
            statsCollector = DEFAULT_NO_STATS_COLLECTOR;
        }
        return statsCollector;
    }

    public EventManagerBuilder setStatsCollector(StatsCollector statsCollector) {
        this.statsCollector = statsCollector;
        return this;
    }

    public String getName() {
        return name;
    }

    public EventManagerBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public EventConnector getEventConnector() {
        if (eventConnector == null) {
            logger.debug("Event Connector is null for {} event bus, creating NoOp Event Connector", getName());
            eventConnector = DEFAULT_NO_EVENT_CONNECTOR;
        }
        return eventConnector;
    }

    public EventManagerBuilder setEventConnector(EventConnector eventConnector) {
        this.eventConnector = eventConnector;
        return this;
    }

    public List<Predicate<Event<Object>>> getEventConnectorPredicates() {
        if (eventConnectorPredicates == null) {
            eventConnectorPredicates = new ArrayList<>();
        }
        return eventConnectorPredicates;
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


        if (eventConnector == null) {
            return getFactory().createEventManager(name, getEventConnector(), getStatsCollector());
        } else {

            if (getEventConnectorPredicates().size() == 0) {
                return getFactory().createEventManager(name, getEventConnector(), getStatsCollector());
            } else {

                Predicate<Event<Object>> mainPredicate = getEventConnectorPredicates().get(0);

                for (int index = 1; index < eventConnectorPredicates.size(); index++) {
                    mainPredicate = mainPredicate.and(eventConnectorPredicates.get(index));
                }
                return getFactory().createEventManager(name,
                        new ConditionalEventConnector(mainPredicate, getEventConnector()), getStatsCollector());
            }

        }
    }

}
