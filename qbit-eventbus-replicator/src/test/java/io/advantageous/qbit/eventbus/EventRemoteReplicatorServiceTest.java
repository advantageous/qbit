package io.advantageous.qbit.eventbus;

import io.advantageous.qbit.Factory;
import io.advantageous.qbit.eventbus.EventRemoteReplicatorService;
import io.advantageous.qbit.events.EventManager;
import io.advantageous.qbit.events.spi.EventConnector;
import io.advantageous.qbit.events.spi.EventTransferObject;
import io.advantageous.qbit.http.jetty.RegisterJettyWithQBit;
import io.advantageous.qbit.spi.FactorySPI;
import io.advantageous.qbit.spi.RegisterBoonWithQBit;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EventRemoteReplicatorServiceTest {


    static {
        RegisterBoonWithQBit.registerBoonWithQBit();
        RegisterJettyWithQBit.registerJettyWithQBit();
    }


    EventRemoteReplicatorService service;
    EventManager eventManager;

    EventTransferObject<Object> event = new EventTransferObject<>("hello", 1L, "TEST.TOPIC");

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

        service.flushConnector();
        verify(eventConnector).flush();
    }


    @Test
    public void testWithSystemBus() {

        Factory factory = mock(Factory.class);
        when(factory.systemEventManager()).thenReturn(eventManager);
        FactorySPI.setFactory(factory);

        service = new EventRemoteReplicatorService();
        service.forwardEvent(event);
        verify(eventManager).forwardEvent(event);
    }



    @Test
    public void testWithConnectorCodeCoverage() {
        EventConnector eventConnector = new EventConnector() {
            @Override
            public void forwardEvent(EventTransferObject<Object> argEvent) {

                assertEquals(event.id(), argEvent.id());
                assertEquals(event.hashCode(), argEvent.hashCode());
                assertEquals(event.isSingleton(), argEvent.isSingleton());
                assertEquals(event.body(), argEvent.body());

                argEvent.headers();
                argEvent.params();


                assertTrue(argEvent.wasReplicated());
                assertTrue(argEvent.equals(event));

            }
        };

        service = new EventRemoteReplicatorService(eventConnector);
        service.forwardEvent(event);
        service.flushConnector();
    }







}