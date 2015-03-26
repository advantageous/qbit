package io.advantageous.qbit.service;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.annotation.EventChannel;
import io.advantageous.qbit.client.ClientProxy;
import io.advantageous.qbit.events.EventBusProxyCreator;
import io.advantageous.qbit.events.EventManager;
import io.advantageous.qbit.test.TimedTesting;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import static io.advantageous.boon.Boon.puts;
import static io.advantageous.boon.Boon.sputs;
import static io.advantageous.qbit.service.ServiceBundleBuilder.serviceBundleBuilder;
import static junit.framework.Assert.assertEquals;

/**
 * Created by rhightower on 3/25/15.
 */
public class ServiceBundleAndEvents extends TimedTesting{


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



    public  class TestServiceImpl implements EventChannel1 {
        public void method1(String arg) {

            method.set(sputs("method1", arg));
            puts("method1", arg);
        }

        @Override
        public void event(String arg) {
            event.set(sputs("GOT EVENT",arg));

            puts("GOT EVENT", arg);
        }
    }


    public static interface TestService extends ClientProxy{
        void method1(String method1);
    }

    @EventChannel
    public  interface EventChannel1 extends ClientProxy{
        public void event(String event);
    }

    @Before
    public void setup() {

        method = new AtomicReference<>();

        event = new AtomicReference<>();


        eventManager = QBit.factory().systemEventManager();
        testServiceImpl = new TestServiceImpl();
        serviceQueue = ServiceBuilder.serviceBuilder()
                .setServiceObject(testServiceImpl).build();

        serviceBundle = serviceBundleBuilder().buildAndStart();



        eventBusProxyCreator = QBit.factory().eventBusProxyCreator();
        sender = eventBusProxyCreator.createProxy(eventManager, EventChannel1.class);



        serviceBundle.addServiceQueue(serviceName, serviceQueue);
        testService = serviceBundle.createLocalProxy(TestService.class, serviceName);

    }


    @Test
    public void test() {

        testService.method1("HELLO");
        testService.clientProxyFlush();

        waitForTrigger(1, o -> (method.get()!=null) &&
                method.get().equals("method1 HELLO"));

        assertEquals("method1 HELLO\n", method.get());

        sender.event("EVENT 1");


        waitForTrigger(4, o -> (event.get()!=null) &&
                event.get().equals("GOT EVENT EVENT 1\n"));


        assertEquals("GOT EVENT EVENT 1\n", event.get());

        sender.event("EVENT 2");


    }

    @After
    public void teardown() {


        Sys.sleep(100);
        serviceBundle.stop();
        serviceQueue.stop();
    }
}
