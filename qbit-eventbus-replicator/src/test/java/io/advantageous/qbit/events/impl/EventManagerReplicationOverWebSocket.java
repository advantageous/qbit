package io.advantageous.qbit.events.impl;

import io.advantageous.qbit.client.Client;
import io.advantageous.qbit.events.EventManager;
import io.advantageous.qbit.events.EventManagerBuilder;
import io.advantageous.qbit.events.spi.EventConnector;
import io.advantageous.qbit.server.ServiceServer;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.service.ServiceProxyUtils;
import io.advantageous.qbit.test.TimedTesting;
import org.boon.core.Sys;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static io.advantageous.qbit.events.impl.EventBusRemoteReplicatorBuilder.eventBusRemoteReplicatorBuilder;
import static io.advantageous.qbit.events.impl.EventBusReplicationClientBuilder.eventBusReplicationClientBuilder;
import static io.advantageous.qbit.service.ServiceBundleBuilder.serviceBundleBuilder;
import static org.junit.Assert.assertEquals;

/**
 * @author rhightower
 */
public class EventManagerReplicationOverWebSocket extends TimedTesting {

    EventConnector replicatorClient;
    ServiceBundle serviceBundleB;
    ServiceBundle serviceBundleA;


    @Test
    public void test() {


        /** Two event managers A and B. Event on A gets replicated to B. */

        EventManager eventManagerA;
        EventManager eventManagerB;

        EventManagerBuilder eventManagerBuilderA = new EventManagerBuilder();
        EventManagerBuilder eventManagerBuilderB = new EventManagerBuilder();


        /** Build B. */
        EventManager eventManagerBImpl = eventManagerBuilderB.build();
        serviceBundleB = serviceBundleBuilder().build(); //build service bundle
        serviceBundleB.addServiceObject("eventManagerB", eventManagerBImpl);
        eventManagerB = serviceBundleB.createLocalProxy(EventManager.class, "eventManagerB"); //wire B to Service Bundle

        EventBusRemoteReplicatorBuilder replicatorBuilder = eventBusRemoteReplicatorBuilder();
        replicatorBuilder.serviceServerBuilder().setPort(9097);
        replicatorBuilder.setEventManager(eventManagerB);
        ServiceServer serviceServer = replicatorBuilder.build();

        EventBusReplicationClientBuilder clientReplicatorBuilder = eventBusReplicationClientBuilder();
        clientReplicatorBuilder.clientBuilder().setPort(9097);
        Client client = clientReplicatorBuilder.build();
        replicatorClient = clientReplicatorBuilder.build(client);

        serviceServer.start();
        client.start();

        /* Create A that connects to the replicator client. */
        EventManager eventManagerAImpl = eventManagerBuilderA.setEventConnector(replicatorClient).build();
        serviceBundleA = serviceBundleBuilder().build(); //build service bundle
        serviceBundleA.addServiceObject("eventManagerA", eventManagerAImpl);
        eventManagerA = serviceBundleA.createLocalProxy(EventManager.class, "eventManagerA"); //wire A to Service Bundle


        serviceBundleA.start();

        serviceBundleB.start();


        final AtomicReference<Object> body = new AtomicReference<>();

        eventManagerB.register("foo.bar", event ->  body.set(event.body()));

        eventManagerA.send("foo.bar", "hello");
        ServiceProxyUtils.flushServiceProxy(eventManagerA);

        waitForTrigger(20, o -> body.get()!=null);


        assertEquals("hello", body.get());

        serviceBundleA.stop();
        serviceBundleB.stop();
        client.stop();
        Sys.sleep(100);
        serviceServer.stop();
    }


}
