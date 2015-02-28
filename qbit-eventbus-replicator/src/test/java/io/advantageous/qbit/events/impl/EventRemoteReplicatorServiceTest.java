package io.advantageous.qbit.events.impl;

import io.advantageous.qbit.Factory;
import io.advantageous.qbit.events.EventConnector;
import io.advantageous.qbit.events.EventManager;
import io.advantageous.qbit.message.Event;
import io.advantageous.qbit.spi.FactorySPI;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EventRemoteReplicatorServiceTest {



    EventRemoteReplicatorService service;
    EventManager eventManager;

    Event<Object> event = new EventImpl<>("hello", 1L, "TEST.TOPIC");

    @Before
    public void setup() {

        eventManager = mock(EventManager.class);

        service = new EventRemoteReplicatorService(eventManager);
    }

    @Test
    public void test() {
        service.forwardEvent(event);
        verify(eventManager).forwardEvent(event);
    }

    @Test
    public void testWithConnector() {
        EventConnector eventConnector = mock(EventConnector.class);
        service = new EventRemoteReplicatorService(eventConnector);
        service.forwardEvent(event);
        verify(eventConnector).forwardEvent(event);
    }


    @Test
    public void testWithSystemBus() {

        Factory factory = mock(Factory.class);
        when(factory.systemEventManager()).thenReturn(eventManager);
        FactorySPI.setFactory(factory);

        service = new EventRemoteReplicatorService(eventManager);
        service.forwardEvent(event);
        verify(eventManager).forwardEvent(event);
    }


}