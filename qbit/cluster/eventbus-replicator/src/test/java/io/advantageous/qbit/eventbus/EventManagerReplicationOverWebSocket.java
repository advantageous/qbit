package io.advantageous.qbit.eventbus;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.client.Client;
import io.advantageous.qbit.events.EventManager;
import io.advantageous.qbit.events.EventManagerBuilder;
import io.advantageous.qbit.events.impl.EventConnectorHub;
import io.advantageous.qbit.events.spi.EventConnector;
import io.advantageous.qbit.server.ServiceEndpointServer;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.service.ServiceProxyUtils;
import io.advantageous.qbit.test.TimedTesting;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static io.advantageous.boon.core.IO.puts;
import static io.advantageous.qbit.eventbus.EventBusRemoteReplicatorBuilder.eventBusRemoteReplicatorBuilder;
import static io.advantageous.qbit.eventbus.EventBusReplicationClientBuilder.eventBusReplicationClientBuilder;
import static io.advantageous.qbit.service.ServiceBundleBuilder.serviceBundleBuilder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author rhightower
 */
public class EventManagerReplicationOverWebSocket extends TimedTesting {


    @Test
    public void test() {


        EventConnectorHub replicatorHubA = new EventConnectorHub();
        EventConnectorHub replicatorHubB = new EventConnectorHub();
        EventConnectorHub replicatorHubC = new EventConnectorHub();


        EventConnector replicatorClientToB;
        EventConnector replicatorClientToC;
        EventConnector replicatorClientToA;
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

        /** Build A. */
        EventManager eventManagerAImpl = eventManagerBuilderA.setEventConnector(replicatorHubA).build("eventBusA");
        serviceBundleA = serviceBundleBuilder().build(); //build service bundle
        serviceBundleA.addServiceObject("eventManagerA", eventManagerAImpl);
        eventManagerA = serviceBundleA.createLocalProxy(EventManager.class, "eventManagerA"); //wire A to Service Bundle


        /** Build B. */
        EventManager eventManagerBImpl = eventManagerBuilderB.setEventConnector(replicatorHubB).build("eventBusB");
        serviceBundleB = serviceBundleBuilder().build(); //build service bundle
        serviceBundleB.addServiceObject("eventManagerB", eventManagerBImpl);
        eventManagerB = serviceBundleB.createLocalProxy(EventManager.class, "eventManagerB"); //wire B to Service Bundle

        /** Build C. */
        EventManager eventManagerCImpl = eventManagerBuilderC.setEventConnector(replicatorHubC).build("eventBusC");
        serviceBundleC = serviceBundleBuilder().build(); //build service bundle
        serviceBundleC.addServiceObject("eventManagerC", eventManagerCImpl);
        eventManagerC = serviceBundleC.createLocalProxy(EventManager.class, "eventManagerC"); //wire C to Service Bundle


        /* A Remote replicator. */
        EventBusRemoteReplicatorBuilder replicatorBuilderA = eventBusRemoteReplicatorBuilder();
        replicatorBuilderA.serviceServerBuilder().setPort(9096);
        replicatorBuilderA.setEventManager(eventManagerA);
        ServiceEndpointServer serviceEndpointServerA = replicatorBuilderA.build();


        /* B remote replicator. */
        EventBusRemoteReplicatorBuilder replicatorBuilderB = eventBusRemoteReplicatorBuilder();
        replicatorBuilderB.serviceServerBuilder().setPort(9097);
        replicatorBuilderB.setEventManager(eventManagerB);
        ServiceEndpointServer serviceEndpointServerB = replicatorBuilderB.build();



        /* C remote replicator. */
        EventBusRemoteReplicatorBuilder replicatorBuilderC = eventBusRemoteReplicatorBuilder();
        replicatorBuilderC.serviceServerBuilder().setPort(9099);
        replicatorBuilderC.setEventManager(eventManagerC);
        ServiceEndpointServer serviceEndpointServerC = replicatorBuilderC.build();



        /* A client replicator */
        EventBusReplicationClientBuilder clientReplicatorBuilder = eventBusReplicationClientBuilder();
        clientReplicatorBuilder.clientBuilder().setPort(9096);
        Client clientA = clientReplicatorBuilder.build();
        replicatorClientToA = clientReplicatorBuilder.build(clientA);


        /* B client replicator */
        clientReplicatorBuilder.clientBuilder().setPort(9097);
        Client clientB = clientReplicatorBuilder.build();
        replicatorClientToB = clientReplicatorBuilder.build(clientB);


        /* C client replicator */
        clientReplicatorBuilder.clientBuilder().setPort(9099);
        Client clientC = clientReplicatorBuilder.build();
        replicatorClientToC = clientReplicatorBuilder.build(clientC);


        replicatorHubA.addAll(replicatorClientToB, replicatorClientToC);
        replicatorHubB.addAll(replicatorClientToA, replicatorClientToC);
        replicatorHubC.addAll(replicatorClientToA, replicatorClientToB);

        Sys.sleep(100);

        serviceEndpointServerB.start();
        serviceEndpointServerC.start();
        serviceEndpointServerA.start();
        clientA.start();
        clientB.start();
        clientC.start();
        serviceBundleA.startUpCallQueue();
        serviceBundleB.startUpCallQueue();
        serviceBundleC.startUpCallQueue();

        Sys.sleep(100);


        final AtomicReference<Object> bodyA = new AtomicReference<>();
        final AtomicReference<Object> bodyB = new AtomicReference<>();
        final AtomicReference<Object> bodyC = new AtomicReference<>();


        eventManagerA.register("foo.bar", event -> bodyA.set(event.body()));
        eventManagerB.register("foo.bar", event -> bodyB.set(event.body()));
        eventManagerC.register("foo.bar", event -> bodyC.set(event.body()));

        eventManagerA.send("foo.bar", "hello");
        ServiceProxyUtils.flushServiceProxy(eventManagerA);
        Sys.sleep(5000);

        ServiceProxyUtils.flushServiceProxy(eventManagerA);

        waitForTrigger(20, o -> bodyA.get() != null && bodyB.get() != null && bodyC.get() != null);
        Sys.sleep(5000);


        puts(bodyA.get(), bodyB.get(), bodyC.get());


        assertEquals("hello", bodyA.get());
        assertEquals("hello", bodyB.get());
        assertEquals("hello", bodyC.get());

        bodyA.set(null);
        bodyB.set(null);
        bodyC.set(null);
        eventManagerC.send("foo.bar", "hello");
        ServiceProxyUtils.flushServiceProxy(eventManagerC);
        Sys.sleep(5000);

        waitForTrigger(20, o -> bodyA.get() != null && bodyB.get() != null);
        Sys.sleep(5000);


        puts(bodyA.get(), bodyB.get(), bodyC.get());


        assertEquals("hello", bodyA.get());
        assertEquals("hello", bodyB.get());


        bodyA.set(null);
        bodyB.set(null);
        bodyC.set(null);
        eventManagerB.send("foo.bar", "hello");
        ServiceProxyUtils.flushServiceProxy(eventManagerB);
        Sys.sleep(5000);

        waitForTrigger(20, o -> bodyA.get() != null && bodyC.get() != null);
        Sys.sleep(5000);

        puts(bodyA.get(), bodyB.get(), bodyC.get());


//        assertEquals("hello", bodyA.get());
//        assertEquals("hello", bodyC.get());


        bodyA.set(null);
        bodyB.set(null);
        bodyC.set(null);
        Sys.sleep(5000);

        assertNull(bodyA.get());
        assertNull(bodyB.get());
        assertNull(bodyC.get());


        serviceBundleA.stop();
        serviceBundleB.stop();
        clientB.stop();
        clientC.stop();
        Sys.sleep(100);

        serviceEndpointServerA.stop();
        serviceEndpointServerB.stop();
        serviceEndpointServerC.stop();

    }


}
