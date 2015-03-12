package io.advantageous.qbit.eventbus;

import io.advantageous.qbit.eventbus.EventRemoteReplicatorService;
import io.advantageous.qbit.events.EventManager;
import io.advantageous.qbit.events.EventManagerBuilder;
import io.advantageous.qbit.events.spi.EventConnector;
import io.advantageous.qbit.http.jetty.RegisterJettyWithQBit;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.service.ServiceProxyUtils;
import io.advantageous.qbit.spi.RegisterBoonWithQBit;
import io.advantageous.qbit.test.TimedTesting;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static io.advantageous.qbit.service.ServiceBundleBuilder.serviceBundleBuilder;
import static org.junit.Assert.assertEquals;

/**
 * Created by rhightower on 3/1/15.
 */
public class EventManagerReplicationIntegrationTest extends TimedTesting{


    EventConnector replicatorClient;
    EventRemoteReplicatorService replicatorService;
    ServiceBundle serviceBundle;

    static {
        RegisterBoonWithQBit.registerBoonWithQBit();
        RegisterJettyWithQBit.registerJettyWithQBit();
    }


    @Test
    public void test() {


        /** Two event managers A and B. Event on A gets replicated to B. */

        EventManager eventManagerA;
        EventManager eventManagerB;

        EventManagerBuilder eventManagerBuilderA = new EventManagerBuilder();
        EventManagerBuilder eventManagerBuilderB = new EventManagerBuilder();


        /** Build B. */
        EventManager eventManagerBImpl = eventManagerBuilderB.build();



        /** replicated to B. */
        serviceBundle = serviceBundleBuilder().build(); //build service bundle

        serviceBundle.addServiceObject("eventManagerB", eventManagerBImpl);
        eventManagerB = serviceBundle.createLocalProxy(EventManager.class, "eventManagerB"); //wire B to Service Bundle

        replicatorService = new EventRemoteReplicatorService(eventManagerB); //Create replicator passing B proxy
        serviceBundle.addServiceObject("eventReplicator", replicatorService); //wire replicator to service bundle
        replicatorClient =
                serviceBundle.createLocalProxy(EventConnector.class, "eventReplicator"); //Create a client proxy to replicator




        /* Create A that connects to the replicator client. */
        EventManager eventManagerAImpl = eventManagerBuilderA.setEventConnector(replicatorClient).build();
        serviceBundle.addServiceObject("eventManagerA", eventManagerAImpl);
        eventManagerA = serviceBundle.createLocalProxy(EventManager.class, "eventManagerA"); //wire A to Service Bundle


        serviceBundle.start();


        final AtomicReference<Object> body = new AtomicReference<>();

        eventManagerB.register("foo.bar", event ->  body.set(event.body()));

        eventManagerA.send("foo.bar", "hello");
        ServiceProxyUtils.flushServiceProxy(eventManagerA);

        waitForTrigger(20, o -> body.get()!=null);


        assertEquals("hello", body.get());

    }
}
