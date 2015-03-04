package io.advantageous.qbit.events.impl;

import io.advantageous.qbit.events.spi.EventConnector;
import io.advantageous.qbit.events.EventManager;
import io.advantageous.qbit.events.spi.EventTransferObject;
import io.advantageous.qbit.message.Event;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.spi.RegisterBoonWithQBit;
import io.advantageous.qbit.test.TimedTesting;
import io.advantageous.qbit.vertx.RegisterVertxWithQBit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.advantageous.qbit.service.ServiceBundleBuilder.serviceBundleBuilder;
import static io.advantageous.qbit.service.ServiceProxyUtils.flushServiceProxy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 */
public class EventRemoteReplicatorServiceBundleIntegrationTest extends TimedTesting {


    static {
        RegisterBoonWithQBit.registerBoonWithQBit();
        RegisterVertxWithQBit.registerVertxWithQBit();
    }



    EventConnector client;
    EventRemoteReplicatorService service;
    EventManager eventManager;
    ServiceBundle serviceBundle;

    EventTransferObject<Object> event = new EventTransferObject<>("hello", 1L, "TEST.TOPIC");

    @Before
    public void setup() {
        setupLatch();
        eventManager = mock(EventManager.class);
        service = new EventRemoteReplicatorService(eventManager);
        serviceBundle = serviceBundleBuilder().build();
        serviceBundle.addServiceObject("remote", service);
        client = serviceBundle.createLocalProxy(EventConnector.class, "remote");
        serviceBundle.start();
    }

    @After
    public void tearDown() {

        serviceBundle.stop();
    }

    @Test
    public void test() {
        client.forwardEvent(event);
        flushServiceProxy(client);
        waitForLatch(1);
        verify(eventManager).forwardEvent(event);

    }

}
