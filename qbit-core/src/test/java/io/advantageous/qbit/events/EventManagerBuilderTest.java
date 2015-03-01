package io.advantageous.qbit.events;

import io.advantageous.qbit.Factory;
import io.advantageous.qbit.events.spi.EventConnector;
import io.advantageous.qbit.events.spi.EventManagerFactory;
import io.advantageous.qbit.events.spi.EventTransferObject;
import io.advantageous.qbit.message.Event;
import io.advantageous.qbit.queue.SendQueue;
import io.advantageous.qbit.service.ServiceQueue;
import io.advantageous.qbit.spi.FactorySPI;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static io.advantageous.qbit.events.EventManagerBuilder.eventManagerBuilder;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EventManagerBuilderTest {


    EventManagerFactory eventManagerFactory;

    Factory factory;

    EventManager eventManager;

    EventConnector eventConnector;

    EventManager eventManagerWithConnector;

    String testChannel = "test.channel";

    EventTransferObject<Object> testEvent = new EventTransferObject<>("body", 1L,  testChannel);


    @Before
    public void setup() {


        eventManager = mock(EventManager.class);
        eventConnector = mock(EventConnector.class);
        eventManagerWithConnector = mock(EventManager.class);


        eventManagerFactory = new EventManagerFactory() {
            @Override
            public EventManager createEventManager() {
                return eventManager;
            }

            @Override
            public EventManager createEventManagerWithConnector(final EventConnector eventConnector) {

                return createEventManagerConnectorShortCut(eventConnector);
            }
        };

        factory = mock(Factory.class);

        when(factory.createEventManager()).thenReturn(eventManagerFactory.createEventManager());


        FactorySPI.setFactory(factory);

    }


    @Test
    public void testWithConnector() {
        EventManagerBuilder eventManagerBuilder = eventManagerBuilder();

        AtomicReference<Event<Object>> eventRef = new AtomicReference<>();

        EventConnector eventConnector = event -> eventRef.set(event);

        when(factory.createEventManagerWithConnector(eventConnector)).thenReturn(
                createEventManagerConnectorShortCut(eventConnector)
        );


        eventManagerBuilder.setEventConnector(eventConnector);


        EventManager build = eventManagerBuilder.build();

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
            public EventManager createEventManagerWithConnector(EventConnector eventConnector) {
                return createEventManagerConnectorShortCut(eventConnector);
            }
        };

        FactorySPI.setFactory(factory);





        eventManagerBuilder.setEventConnector(eventConnector);

        eventManagerBuilder.addEventConnectorPredicate(objectEvent -> false);

        EventManager build = eventManagerBuilder.build();

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
            public EventManager createEventManagerWithConnector(EventConnector eventConnector) {
                return createEventManagerConnectorShortCut(eventConnector);
            }
        };

        FactorySPI.setFactory(factory);





        eventManagerBuilder.setEventConnector(eventConnector);

        eventManagerBuilder.addEventConnectorPredicate(objectEvent -> true);

        EventManager build = eventManagerBuilder.build();

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
            public EventManager createEventManagerWithConnector(EventConnector eventConnector) {
                return createEventManagerConnectorShortCut(eventConnector);
            }
        };

        FactorySPI.setFactory(factory);





        eventManagerBuilder.setEventConnector(eventConnector);

        eventManagerBuilder.addEventConnectorPredicate(objectEvent -> true);

        eventManagerBuilder.addEventConnectorPredicate(objectEvent -> true);

        EventManager build = eventManagerBuilder.build();

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
            public EventManager createEventManagerWithConnector(EventConnector eventConnector) {
                return createEventManagerConnectorShortCut(eventConnector);
            }
        };

        FactorySPI.setFactory(factory);





        eventManagerBuilder.setEventConnector(eventConnector);

        eventManagerBuilder.addEventConnectorPredicate(objectEvent -> true);

        eventManagerBuilder.addEventConnectorPredicate(objectEvent -> true);

        eventManagerBuilder.addEventConnectorPredicate(objectEvent -> false);

        EventManager build = eventManagerBuilder.build();

        build.forwardEvent(testEvent);




        assertNull(eventRef.get());
    }



    private EventManager createEventManagerConnectorShortCut(final EventConnector eventConnector) {
        return new EventManager(){

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
        public <T> void subscribe(String channelName, SendQueue<Event<Object>> event) {

        }

        @Override
        public <T> void consume(String channelName, SendQueue<Event<Object>> event) {

        }

        @Override
        public <T> void send(String channel, T event) {

        }

        @Override
        public <T> void sendArray(String channel, T... event) {

        }

        @Override
        public <T> void sendCopy(String channel, T event) {

        }

        @Override
        public <T> void forwardEvent(EventTransferObject<Object> event) {
            eventConnector.forwardEvent(event);
        }
    };
    }
}