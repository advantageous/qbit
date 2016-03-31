package io.advantageous.qbit.events;

import io.advantageous.qbit.Factory;
import io.advantageous.qbit.events.spi.EventConnector;
import io.advantageous.qbit.events.spi.EventManagerFactory;
import io.advantageous.qbit.events.spi.EventTransferObject;
import io.advantageous.qbit.message.Event;
import io.advantageous.qbit.service.ServiceQueue;
import io.advantageous.qbit.service.stats.StatsCollector;
import io.advantageous.qbit.spi.FactorySPI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static io.advantageous.qbit.events.EventManagerBuilder.*;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EventManagerBuilderTest {

    EventManagerFactory eventManagerFactory;
    Factory factory;
    EventManager eventManager;
    EventConnector eventConnector;
    EventManager eventManagerWithConnector;
    String testChannel = "test.channel";
    EventTransferObject<Object> testEvent = new EventTransferObject<>("body", 1L, testChannel);


    @Before
    public void setup() {


        eventManager = mock(EventManager.class);
        eventConnector = mock(EventConnector.class);
        eventManagerWithConnector = mock(EventManager.class);


        eventManagerFactory = new EventManagerFactory() {


            @Override
            public EventManager createEventManager(String name,
                                                   EventConnector eventConnector,
                                                   StatsCollector statsCollector) {

                if (eventConnector == EventManagerBuilder.DEFAULT_NO_EVENT_CONNECTOR) {
                    return eventManager;
                } else {
                    return createEventManagerConnectorShortCut(name, eventConnector);
                }
            }


            public EventManager createEventManagerWithConnector(String name, final EventConnector eventConnector) {

                return createEventManagerConnectorShortCut(name, eventConnector);
            }
        };

        factory = mock(Factory.class);

        when(factory.createEventManager("foo", DEFAULT_NO_EVENT_CONNECTOR, DEFAULT_NO_STATS_COLLECTOR))
                .thenReturn(eventManagerFactory.createEventManager("foo", DEFAULT_NO_EVENT_CONNECTOR, DEFAULT_NO_STATS_COLLECTOR));


        FactorySPI.setFactory(factory);

    }


    @After
    public void teardown() {

        FactorySPI.setFactory(null);
    }

    @Test
    public void testWithConnector() {
        EventManagerBuilder eventManagerBuilder = eventManagerBuilder();

        AtomicReference<Event<Object>> eventRef = new AtomicReference<>();

        EventConnector eventConnector = event -> eventRef.set(event);

        eventManagerBuilder.setEventConnector(eventConnector);


        EventManager build = eventManagerBuilder.setFactory(new Factory() {
            @Override
            public EventManager createEventManager(String name, EventConnector eventConnector, StatsCollector statsCollector) {
                return createEventManagerConnectorShortCut("foo", eventConnector);
            }
        }).build("foo");

        build.forwardEvent(testEvent);


        assertSame(testEvent, eventRef.get());
    }


    @Test
    public void testWithPredicateFalse() {
        EventManagerBuilder eventManagerBuilder = eventManagerBuilder();

        AtomicReference<Event<Object>> eventRef = new AtomicReference<>();

        EventConnector eventConnector = event -> eventRef.set(event);


        factory = new Factory() {


            @Override
            public EventManager createEventManager(String name,
                                                   EventConnector eventConnector,
                                                   StatsCollector statsCollector) {

                if (eventConnector == EventManagerBuilder.DEFAULT_NO_EVENT_CONNECTOR) {
                    return eventManager;
                } else {
                    return createEventManagerConnectorShortCut(name, eventConnector);
                }
            }

            public EventManager createEventManagerWithConnector(String name, EventConnector eventConnector) {
                return createEventManagerConnectorShortCut("foo", eventConnector);
            }
        };

        FactorySPI.setFactory(factory);


        eventManagerBuilder.setEventConnector(eventConnector);

        eventManagerBuilder.addEventConnectorPredicate(objectEvent -> false);

        EventManager build = eventManagerBuilder.build("foo");

        build.forwardEvent(testEvent);


        assertNull(eventRef.get());
    }


    @Test
    public void testWithPredicateTrue() {
        EventManagerBuilder eventManagerBuilder = eventManagerBuilder();

        AtomicReference<Event<Object>> eventRef = new AtomicReference<>();

        EventConnector eventConnector = event -> eventRef.set(event);


        factory = new Factory() {


            @Override
            public EventManager createEventManager(String name,
                                                   EventConnector eventConnector,
                                                   StatsCollector statsCollector) {

                if (eventConnector == EventManagerBuilder.DEFAULT_NO_EVENT_CONNECTOR) {
                    return eventManager;
                } else {
                    return createEventManagerConnectorShortCut(name, eventConnector);
                }
            }


            public EventManager createEventManagerWithConnector(String name, EventConnector eventConnector) {
                return createEventManagerConnectorShortCut(name, eventConnector);
            }
        };

        FactorySPI.setFactory(factory);


        eventManagerBuilder.setEventConnector(eventConnector);

        eventManagerBuilder.addEventConnectorPredicate(objectEvent -> true);

        EventManager build = eventManagerBuilder.build("foo");

        build.forwardEvent(testEvent);


        assertSame(testEvent, eventRef.get());
    }


    @Test
    public void testWithPredicateTwoTrue() {
        EventManagerBuilder eventManagerBuilder = eventManagerBuilder();

        AtomicReference<Event<Object>> eventRef = new AtomicReference<>();

        EventConnector eventConnector = event -> eventRef.set(event);


        factory = new Factory() {


            @Override
            public EventManager createEventManager(String name,
                                                   EventConnector eventConnector,
                                                   StatsCollector statsCollector) {

                if (eventConnector == EventManagerBuilder.DEFAULT_NO_EVENT_CONNECTOR) {
                    return eventManager;
                } else {
                    return createEventManagerConnectorShortCut(name, eventConnector);
                }
            }

            public EventManager createEventManagerWithConnector(String name, EventConnector eventConnector) {
                return createEventManagerConnectorShortCut(name, eventConnector);
            }
        };

        FactorySPI.setFactory(factory);


        eventManagerBuilder.setEventConnector(eventConnector);

        eventManagerBuilder.addEventConnectorPredicate(objectEvent -> true);

        eventManagerBuilder.addEventConnectorPredicate(objectEvent -> true);

        EventManager build = eventManagerBuilder.build("foo");

        build.forwardEvent(testEvent);


        assertSame(testEvent, eventRef.get());
    }


    @Test
    public void testWithPredicateTwoTrue1False() {
        EventManagerBuilder eventManagerBuilder = eventManagerBuilder();

        AtomicReference<Event<Object>> eventRef = new AtomicReference<>();

        EventConnector eventConnector = event -> eventRef.set(event);


        factory = new Factory() {


            @Override
            public EventManager createEventManager(String name,
                                                   EventConnector eventConnector,
                                                   StatsCollector statsCollector) {

                if (eventConnector == EventManagerBuilder.DEFAULT_NO_EVENT_CONNECTOR) {
                    return eventManager;
                } else {
                    return createEventManagerConnectorShortCut(name, eventConnector);
                }
            }

            public EventManager createEventManagerWithConnector(String name, EventConnector eventConnector) {
                return createEventManagerConnectorShortCut(name, eventConnector);
            }
        };

        FactorySPI.setFactory(factory);


        eventManagerBuilder.setEventConnector(eventConnector);

        eventManagerBuilder.addEventConnectorPredicate(objectEvent -> true);

        eventManagerBuilder.addEventConnectorPredicate(objectEvent -> true);

        eventManagerBuilder.addEventConnectorPredicate(objectEvent -> false);

        EventManager build = eventManagerBuilder.build("foo");

        build.forwardEvent(testEvent);


        assertNull(eventRef.get());
    }


    private EventManager createEventManagerConnectorShortCut(final String name, final EventConnector eventConnector) {
        return new EventManager() {

            @Override
            public void joinService(ServiceQueue serviceQueue) {

            }

            @Override
            public void leave() {

            }

            @Override
            public void listen(Object listener) {

            }

            @Override
            public void stopListening(Object listener) {

            }

            @Override
            public <T> void register(String channelName, EventListener<T> listener) {

            }

            @Override
            public <T> void unregister(String channelName, EventListener<T> listener) {

            }


            @Override
            public <T> void send(String channel, T event) {

            }

            @Override
            public <T> void sendArray(String channel, T... event) {

            }

            @Override
            public <T> void sendArguments(String channel, T... event) {

            }

            @Override
            public <T> void sendCopy(String channel, T event) {

            }

            @Override
            public void forwardEvent(EventTransferObject<Object> event) {
                eventConnector.forwardEvent(event);
            }
        };
    }
}