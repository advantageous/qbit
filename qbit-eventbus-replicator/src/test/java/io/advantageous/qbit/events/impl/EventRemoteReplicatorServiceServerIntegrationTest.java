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

    public int useOneOfThese(int... ports) throws IOException {
        for (int port : ports) {
            try {
                ServerSocket serverSocket = new ServerSocket(port);
                return port;
            } catch (IOException ex) {
                continue; // try next port
            }
        }
        // if the program gets here, no port in the range was found
        throw new IOException("no free port found");
    }


    @Before
    public void setup() throws IOException {

        setupLatch();
        eventManager = mock(EventManager.class);
        service = new EventRemoteReplicatorService(eventManager);



        int port = useOneOfThese(8080,7070,6060,6666,5555,4444, 2121, 8081, 8082, 7777, 6767, 2323, 5555);
        serviceServer = serviceServerBuilder().setPort(port).build();
        serviceServer.initServices(service);
        client = clientBuilder().build();


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
