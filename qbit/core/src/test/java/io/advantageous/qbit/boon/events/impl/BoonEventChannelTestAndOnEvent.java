package io.advantageous.qbit.boon.events.impl;

import io.advantageous.qbit.QBit;
import io.advantageous.qbit.annotation.EventChannel;
import io.advantageous.qbit.annotation.OnEvent;
import io.advantageous.qbit.events.EventBusProxyCreator;
import io.advantageous.qbit.events.EventManager;
import io.advantageous.qbit.service.ServiceQueue;
import io.advantageous.qbit.test.TimedTesting;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static io.advantageous.qbit.service.ServiceBuilder.serviceBuilder;
import static junit.framework.Assert.assertEquals;

/**
 * Boon Event Channel Test for On event
 * created by rhightower on 3/18/15.
 */
public class BoonEventChannelTestAndOnEvent extends TimedTesting {

    public AtomicInteger eventCount = new AtomicInteger();

    @Test
    public void test() throws Exception {

        eventCount.set(0);

        //Create the event bus and the channel
        final EventManager eventManager = QBit.factory().systemEventManager();
        final EventBusProxyCreator eventBusProxyCreator = QBit.factory().eventBusProxyCreator();
        /* Create a channel. */
        final MyChannelInterface channel = eventBusProxyCreator.createProxy(eventManager, MyChannelInterface.class);

        //Sender service, impl, serviceQueue and client proxy.
        /* Create the sender service. */
        final MyServiceEventSender serviceSender = new MyServiceEventSender(channel);
        /* Create the service queue for the sender. */
        final ServiceQueue serviceSenderQueue = serviceBuilder().setServiceObject(serviceSender).build();
        /* Create the client interface for the sender. */
        final MyServiceInterface serverSenderClient = serviceSenderQueue.createProxyWithAutoFlush(
                MyServiceInterface.class, 100, TimeUnit.MILLISECONDS);


        //Create the receiver services.
        final MyServiceEventReceiver receiverService = new MyServiceEventReceiver();
        final MyServiceEventReceiver2 receiverService2 = new MyServiceEventReceiver2();
        final MyServiceEventReceiver3 receiverService3 = new MyServiceEventReceiver3();
        final ServiceQueue receiverServiceQueue = serviceBuilder().setServiceObject(receiverService).build();
        final ServiceQueue receiverServiceQueue2 = serviceBuilder().setServiceObject(receiverService2).build();
        final ServiceQueue receiverServiceQueue3 = serviceBuilder().setServiceObject(receiverService3).build();


        /*Start the services. */
        serviceSenderQueue.start();
        receiverServiceQueue.start();
        receiverServiceQueue2.start();
        receiverServiceQueue3.start();


        /* Now send a message with the client. */
        serverSenderClient.someServiceMethod();


        waitForTrigger(5, o -> eventCount.get() == 3);

        assertEquals(3, eventCount.get());


        serviceSenderQueue.stop();
        receiverServiceQueue.stop();
        receiverServiceQueue2.stop();
        receiverServiceQueue3.stop();

    }


    @EventChannel("FOO")
    interface MyChannelInterface {

        @EventChannel("bam")
        void somethingHappened(int i, String foo);
    }

    interface MyServiceInterface {

        void someServiceMethod();
    }

    static class MyServiceEventSender {

        final MyChannelInterface channel;

        MyServiceEventSender(MyChannelInterface channel) {
            this.channel = channel;
        }

        public void someServiceMethod() {
            channel.somethingHappened(1, "foo");
        }
    }

    class MyServiceEventReceiver implements MyChannelInterface {

        @Override
        public void somethingHappened(int i, String foo) {

            eventCount.incrementAndGet();
            System.out.println("MyServiceEventReceiver bar" + i + " foo " + foo);
        }
    }

    class MyServiceEventReceiver2 implements MyChannelInterface {

        @Override
        public void somethingHappened(int i, String foo) {

            eventCount.incrementAndGet();

            System.out.println("MyServiceEventReceiver2 bar2" + i + " foo " + foo);

        }
    }

    class MyServiceEventReceiver3 {

        //@OnEvent("io.advantageous.qbit.boon.events.impl.BoonEventChannelTestAndOnEvent$MyChannelInterface.somethingHappened")
        @OnEvent("FOO.bam")
        public void onSomethingHappened(int i, String foo) {

            eventCount.incrementAndGet();

            System.out.println("MyServiceEventReceiver2 bar2" + i + " foo " + foo);

        }
    }

}
