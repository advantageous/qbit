package io.advantageous.qbit.eventbus;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.boon.spi.RegisterBoonWithQBit;
import io.advantageous.qbit.client.Client;
import io.advantageous.qbit.events.EventManager;
import io.advantageous.qbit.events.spi.EventConnector;
import io.advantageous.qbit.events.spi.EventTransferObject;
import io.advantageous.qbit.server.ServiceEndpointServer;
import io.advantageous.qbit.test.TimedTesting;
import io.advantageous.qbit.util.PortUtils;
import io.advantageous.qbit.vertx.RegisterVertxWithQBit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static io.advantageous.boon.core.IO.puts;
import static io.advantageous.qbit.client.ClientBuilder.clientBuilder;
import static io.advantageous.qbit.server.EndpointServerBuilder.endpointServerBuilder;
import static io.advantageous.qbit.service.ServiceProxyUtils.flushServiceProxy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

//import io.advantageous.qbit.http.jetty.RegisterJettyWithQBit;

/**
 */
public class EventRemoteReplicatorServiceEndpointServerIntegrationTest extends TimedTesting {


    static {
        RegisterBoonWithQBit.registerBoonWithQBit();
        //RegisterJettyWithQBit.registerJettyWithQBit();
    }


    EventConnector clientEventConnector;
    EventRemoteReplicatorService service;
    EventManager eventManager;
    ServiceEndpointServer serviceEndpointServer;
    Client client;


    EventTransferObject<Object> event = new EventTransferObject<>("hello", 1L, "TEST.TOPIC");


    @Before
    public void setup() throws IOException {

        setupLatch();

        RegisterVertxWithQBit.registerVertxWithQBit();
        RegisterBoonWithQBit.registerBoonWithQBit();
        eventManager = mock(EventManager.class);
        service = new EventRemoteReplicatorService(eventManager);


        Sys.sleep(1000);
        int port = PortUtils.useOneOfThesePorts(3000, 6000, 4000, 3000);

        puts("findOpenPort(", port, ")");

        serviceEndpointServer = endpointServerBuilder().setPort(port).build();
        serviceEndpointServer.initServices(service);
        client = clientBuilder().setPort(port).build();

        Sys.sleep(1000);
        serviceEndpointServer.start();
        Sys.sleep(1000);
        client.start();

        clientEventConnector = client.createProxy(EventConnector.class, "eventRemoteReplicatorService");
        Sys.sleep(100);
    }


    @After
    public void tearDown() {

        if (client != null) {
            client.stop();
        }

        if (serviceEndpointServer != null) {
            serviceEndpointServer.stop();
        }
    }


    @Test
    public void test() {
        clientEventConnector.forwardEvent(event);
        flushServiceProxy(clientEventConnector);
        Sys.sleep(100);

        waitForLatch(2);
        client.flush();
        waitForLatch(2);
        Sys.sleep(100);

        verify(eventManager).forwardEvent(event);

    }

}
