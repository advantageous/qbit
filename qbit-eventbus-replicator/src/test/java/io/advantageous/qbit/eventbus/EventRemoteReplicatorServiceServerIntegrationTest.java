package io.advantageous.qbit.eventbus;

import io.advantageous.qbit.client.Client;
import io.advantageous.qbit.eventbus.EventRemoteReplicatorService;
import io.advantageous.qbit.events.EventManager;
import io.advantageous.qbit.events.spi.EventConnector;
import io.advantageous.qbit.events.spi.EventTransferObject;
import io.advantageous.qbit.http.jetty.RegisterJettyWithQBit;
import io.advantageous.qbit.server.ServiceServer;
import io.advantageous.qbit.spi.RegisterBoonWithQBit;
import io.advantageous.qbit.test.TimedTesting;
import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.util.PortUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static io.advantageous.boon.Boon.puts;
import static io.advantageous.qbit.client.ClientBuilder.clientBuilder;
import static io.advantageous.qbit.server.ServiceServerBuilder.serviceServerBuilder;
import static io.advantageous.qbit.service.ServiceProxyUtils.flushServiceProxy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 */
public class EventRemoteReplicatorServiceServerIntegrationTest extends TimedTesting {


    static {
        RegisterBoonWithQBit.registerBoonWithQBit();
        RegisterJettyWithQBit.registerJettyWithQBit();
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


        Sys.sleep(1000);
        int port = PortUtils.useOneOfThesePorts(3000, 6000, 4000, 3000);

        puts("findOpenPort(", port, ")");

        serviceServer = serviceServerBuilder().setPort(port).build();
        serviceServer.initServices(service);
        client = clientBuilder().setPort(port).build();

        Sys.sleep(1000);
        serviceServer.start();
        Sys.sleep(1000);
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
