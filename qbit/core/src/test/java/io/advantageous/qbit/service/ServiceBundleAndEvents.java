package io.advantageous.qbit.service;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.annotation.EventChannel;
import io.advantageous.qbit.client.ClientProxy;
import io.advantageous.qbit.events.EventBusProxyCreator;
import io.advantageous.qbit.events.EventManager;
import io.advantageous.qbit.events.EventManagerBuilder;
import io.advantageous.qbit.test.TimedTesting;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static io.advantageous.boon.core.IO.puts;
import static io.advantageous.boon.core.Str.sputs;
import static io.advantageous.qbit.service.ServiceBundleBuilder.serviceBundleBuilder;
import static junit.framework.Assert.assertEquals;

/**
 * created by rhightower on 3/25/15.
 */
public class ServiceBundleAndEvents extends TimedTesting {


    final String serviceName = "testService";
    EventManager eventManager;
    ServiceBundle serviceBundle;
    ServiceQueue serviceQueue;
    TestServiceImpl testServiceImpl;
    TestService testService;
    EventChannel1 sender;

    EventBusProxyCreator eventBusProxyCreator;

    AtomicReference<String> method = new AtomicReference<>();

    AtomicReference<String> event = new AtomicReference<>();

    ServiceQueue eventServiceQueue;

    @Before
    public void setup() {

        method = new AtomicReference<>();

        event = new AtomicReference<>();


        eventManager = EventManagerBuilder.eventManagerBuilder().build("localtest");
        eventServiceQueue = ServiceBuilder.serviceBuilder().setServiceObject(eventManager).buildAndStartAll();

        eventManager = eventServiceQueue.createProxy(EventManager.class);

        testServiceImpl = new TestServiceImpl();
        ServiceBuilder serviceBuilder = ServiceBuilder.serviceBuilder().setEventManager(eventManager);
        serviceBuilder.getRequestQueueBuilder().setBatchSize(100);

        this.serviceQueue = serviceBuilder
                .setServiceObject(testServiceImpl).buildAndStart();

        serviceBundle = serviceBundleBuilder().buildAndStart();


        eventBusProxyCreator = QBit.factory().eventBusProxyCreator();
        sender = eventBusProxyCreator.createProxy(eventManager, EventChannel1.class);


        serviceBundle.addServiceQueue(serviceName, this.serviceQueue);
        testService = serviceBundle.createLocalProxy(TestService.class, serviceName);

    }

    @Test
    public void test() {

        testService.method1("HELLO");
        testService.clientProxyFlush();

        waitForTrigger(1, o -> (method.get() != null) &&
                method.get().equals("method1 HELLO"));

        assertEquals("method1 HELLO\n", method.get());

        sender.event("EVENT 1");
        sender.clientProxyFlush();


        waitForTrigger(1, o -> (event.get() != null) &&
                event.get().equals("GOT EVENT EVENT 1\n"));


        assertEquals("GOT EVENT EVENT 1\n", event.get());


    }

    @After
    public void teardown() {


        Sys.sleep(100);
        serviceBundle.stop();
        serviceQueue.stop();
        eventServiceQueue.stop();
    }

    public interface TestService extends ClientProxy {
        void method1(String method1);
    }


    @EventChannel
    public interface EventChannel1 extends ClientProxy {
        void event(String event);
    }

    public class TestServiceImpl implements EventChannel1 {
        public void method1(String arg) {
            puts("method1", arg);
            method.set(sputs("method1", arg));
        }

        @Override
        public void event(String arg) {

            puts("GOT EVENT", arg);
            event.set(sputs("GOT EVENT", arg));

        }
    }
}
