package io.advantageous.qbit.events.impl;

import io.advantageous.qbit.client.Client;
import io.advantageous.qbit.events.spi.EventConnector;
import io.advantageous.qbit.events.EventManager;
import io.advantageous.qbit.events.spi.EventTransferObject;
import io.advantageous.qbit.message.Event;
import io.advantageous.qbit.server.ServiceServer;
import io.advantageous.qbit.spi.RegisterBoonWithQBit;
import io.advantageous.qbit.test.TimedTesting;
import io.advantageous.qbit.vertx.RegisterVertxWithQBit;
import io.advantageous.boon.core.Sys;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.ServerSocket;

import static io.advantageous.boon.Boon.puts;
import static io.advantageous.qbit.client.ClientBuilder.clientBuilder;
import static io.advantageous.qbit.server.ServiceServerBuilder.serviceServerBuilder;
import static io.advantageous.qbit.service.ServiceProxyUtils.flushServiceProxy;
import static io.advantageous.qbit.util.PortUtils.findOpenPort;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 */
public class EventRemoteReplicatorServiceServerIntegrationTest extends TimedTesting {


    static {
        RegisterBoonWithQBit.registerBoonWithQBit();
        RegisterVertxWithQBit.registerVertxWithQBit();
    }



    EventConnector clientEventConnector;
    EventRemoteReplicatorService service;
    EventManager eventManager;
    ServiceServer serviceServer;
    Client client;



    EventTransferObject<Object> event = new EventTransferObject<>("hello", 1L, "TEST.TOPIC");



    @Before
    public void setup() throws IOException {

        setupLatch();
        eventManager = mock(EventManager.class);
        service = new EventRemoteReplicatorService(eventManager);


        int port = findOpenPort();

        puts(port);

        serviceServer = serviceServerBuilder().setPort(port).build();
        serviceServer.initServices(service);
        client = clientBuilder().setPort(port).build();


        serviceServer.start();
        Sys.sleep(100);
        client.start();

        clientEventConnector  = client.createProxy(EventConnector.class, "eventRemoteReplicatorService");
    }

    @After
    public void tearDown() {
        client.stop();
        serviceServer.stop();
    }


    @Test
    public void test() {
        clientEventConnector.forwardEvent(event);
        flushServiceProxy(clientEventConnector);
        waitForLatch(1);
        client.flush();
        waitForLatch(1);
        verify(eventManager).forwardEvent(event);

    }

}
