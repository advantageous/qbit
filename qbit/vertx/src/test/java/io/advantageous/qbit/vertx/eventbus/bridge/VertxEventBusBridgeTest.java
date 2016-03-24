package io.advantageous.qbit.vertx.eventbus.bridge;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.service.ServiceBuilder;
import io.advantageous.qbit.service.ServiceQueue;
import io.vertx.core.Vertx;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;


public class VertxEventBusBridgeTest {

    public static class TestService {
        AtomicReference<String> value = new AtomicReference<>();
        public boolean test(final String newValue) {

            System.out.println("HERE::" + newValue);
            value.set(newValue);

            return true;
        }
    }

    public  interface ITestService {
        void test(Callback<Boolean> callback, final String newValue, final CountDownLatch countDownLatch);
    }


    @Test
    public void test() throws Exception {

        final TestService testService = new TestService();



        final ServiceBuilder serviceBuilder = ServiceBuilder.serviceBuilder();
        serviceBuilder.setServiceObject(testService);
        final ServiceQueue serviceQueue = serviceBuilder.build();

        final VertxEventBusBridgeBuilder vertxEventBusBridgeBuilder = VertxEventBusBridgeBuilder.vertxEventBusBridgeBuilder();
        vertxEventBusBridgeBuilder.addBridgeAddress("testservice");

        vertxEventBusBridgeBuilder.setServiceQueue(serviceQueue);

        serviceQueue.startAll();
        final ITestService proxy = serviceQueue.createProxyWithAutoFlush(ITestService.class, 1, TimeUnit.MILLISECONDS);

        final CountDownLatch countDownLatch = new CountDownLatch(1);
        proxy.test(aBoolean -> System.out.println("HERE::AFTER"), "FOO", countDownLatch);
        countDownLatch.await();

        Vertx vertx = vertxEventBusBridgeBuilder.getVertx();

        vertx.eventBus().send("testservice", "{'method':'test', }");

    }

}