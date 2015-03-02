package io.advantageous.qbit.events.impl;

import io.advantageous.qbit.client.Client;
import io.advantageous.qbit.events.EventManager;
import io.advantageous.qbit.events.EventManagerBuilder;
import io.advantageous.qbit.events.spi.EventConnector;
import io.advantageous.qbit.server.ServiceServer;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.service.ServiceProxyUtils;
import io.advantageous.qbit.test.TimedTesting;
import org.boon.Lists;
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


    @Test
    public void test() {


        EventConnector replicatorHub;
        EventConnector replicatorClientA1;
        EventConnector replicatorClientA2;
        ServiceBundle serviceBundleB;
        ServiceBundle serviceBundleA;
        ServiceBundle serviceBundleC;


        /** Two event managers A and B. Event on A gets replicated to B. */

        EventManager eventManagerA;
        EventManager eventManagerB;
        EventManager eventManagerC;

        EventManagerBuilder eventManagerBuilderA = new EventManagerBuilder();
        EventManagerBuilder eventManagerBuilderB = new EventManagerBuilder();
        EventManagerBuilder eventManagerBuilderC = new EventManagerBuilder();


        /** Build B. */
        EventManager eventManagerBImpl = eventManagerBuilderB.build();
        serviceBundleB = serviceBundleBuilder().build(); //build service bundle
        serviceBundleB.addServiceObject("eventManagerB", eventManagerBImpl);
        eventManagerB = serviceBundleB.createLocalProxy(EventManager.class, "eventManagerB"); //wire B to Service Bundle

        /** Build C. */
        EventManager eventManagerCImpl = eventManagerBuilderC.build();
        serviceBundleC = serviceBundleBuilder().build(); //build service bundle
        serviceBundleC.addServiceObject("eventManagerC", eventManagerCImpl);
        eventManagerC = serviceBundleC.createLocalProxy(EventManager.class, "eventManagerC"); //wire C to Service Bundle


        EventBusRemoteReplicatorBuilder replicatorBuilderB = eventBusRemoteReplicatorBuilder();
        replicatorBuilderB.serviceServerBuilder().setPort(9097);
        replicatorBuilderB.setEventManager(eventManagerB);
        ServiceServer serviceServerB = replicatorBuilderB.build();


        EventBusRemoteReplicatorBuilder replicatorBuilderC = eventBusRemoteReplicatorBuilder();
        replicatorBuilderC.serviceServerBuilder().setPort(9099);
        replicatorBuilderC.setEventManager(eventManagerB);
        ServiceServer serviceServerC = replicatorBuilderC.build();

        EventBusReplicationClientBuilder clientReplicatorBuilder = eventBusReplicationClientBuilder();
        clientReplicatorBuilder.clientBuilder().setPort(9097);
        Client clientB = clientReplicatorBuilder.build();
        replicatorClientA1 = clientReplicatorBuilder.build(clientB);


        clientReplicatorBuilder.clientBuilder().setPort(9099);
        Client clientC = clientReplicatorBuilder.build();
        replicatorClientA2 = clientReplicatorBuilder.build(clientC);

        serviceServerB.start();
        serviceServerC.start();
        clientB.start();
        clientC.start();

        replicatorHub = new EventConnectorHub(Lists.list(replicatorClientA1, replicatorClientA2));

        /* Create A that connects to the replicator client. */
        EventManager eventManagerAImpl = eventManagerBuilderA.setEventConnector(replicatorHub).build();
        serviceBundleA = serviceBundleBuilder().build(); //build service bundle
        serviceBundleA.addServiceObject("eventManagerA", eventManagerAImpl);
        eventManagerA = serviceBundleA.createLocalProxy(EventManager.class, "eventManagerA"); //wire A to Service Bundle


        serviceBundleA.start();

        serviceBundleB.start();


        final AtomicReference<Object> bodyB = new AtomicReference<>();

        final AtomicReference<Object> bodyC = new AtomicReference<>();

        eventManagerB.register("foo.bar", event ->  bodyB.set(event.body()));
        eventManagerB.register("foo.bar", event ->  bodyC.set(event.body()));

        eventManagerA.send("foo.bar", "hello");
        ServiceProxyUtils.flushServiceProxy(eventManagerA);

        waitForTrigger(20, o -> bodyB.get()!=null && bodyC.get()!=null);

        assertEquals("hello", bodyB.get());
        assertEquals("hello", bodyC.get());

        serviceBundleA.stop();
        serviceBundleB.stop();
        clientB.stop();
        clientC.stop();
        Sys.sleep(100);
        serviceServerB.stop();
        serviceServerC.stop();
    }


}
