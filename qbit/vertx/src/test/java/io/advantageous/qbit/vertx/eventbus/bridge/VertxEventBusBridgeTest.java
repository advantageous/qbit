package io.advantageous.qbit.vertx.eventbus.bridge;

import io.advantageous.qbit.service.ServiceBuilder;
import io.advantageous.qbit.service.ServiceQueue;
import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;


public class VertxEventBusBridgeTest {

    @Test
    public void test() throws Exception {

        /* test service */
        final TestService testService = new TestService();

        /* address */
        final String address = "testservice";

        /* service builder */
        final ServiceBuilder serviceBuilder = ServiceBuilder.serviceBuilder();
        serviceBuilder.setServiceObject(testService);
        final ServiceQueue serviceQueue = serviceBuilder.build();

        /* vertx event bus bridge to qbit. */
        final VertxEventBusBridgeBuilder vertxEventBusBridgeBuilder = VertxEventBusBridgeBuilder.vertxEventBusBridgeBuilder();
        vertxEventBusBridgeBuilder.addBridgeAddress(address);
        vertxEventBusBridgeBuilder.setServiceQueue(serviceQueue);
        serviceQueue.startAll(); //startall not supported yet for bridge.
        vertxEventBusBridgeBuilder.build();


        /* latch so we can test results coming back from bridge. */
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        /* grab vertx from the bridge. */
        final Vertx vertx = vertxEventBusBridgeBuilder.getVertx();

        final AtomicReference<AsyncResult<Message<Object>>> ref = new AtomicReference<>();

        /* calling using the event bus, and use latch to wait for the return. */
        vertx.eventBus().send(address, "{'method':'test', 'args':['hello']}".replaceAll("'", "\""),
                reply -> {
                    ref.set(reply);
                    countDownLatch.countDown();
                });

        countDownLatch.await();

        assertEquals("hello", testService.value.get());

        assertEquals("true", ref.get().result().body());

        vertx.close();
    }


    public static class TestService {
        AtomicReference<String> value = new AtomicReference<>();

        public boolean test(final String newValue) {

            System.out.println("HERE::" + newValue);
            value.set(newValue);

             return true;
        }
    }

}