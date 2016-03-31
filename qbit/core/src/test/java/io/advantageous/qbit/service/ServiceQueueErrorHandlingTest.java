package io.advantageous.qbit.service;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.annotation.QueueCallback;
import io.advantageous.qbit.annotation.QueueCallbackType;
import io.advantageous.qbit.reactive.Callback;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.Assert.assertEquals;


public class ServiceQueueErrorHandlingTest {


    private ServiceBuilder serviceQueueBuilder;

    @Before
    public void setup() {

        serviceQueueBuilder = ServiceBuilder.serviceBuilder();

    }

    //Seems to work locally.
    @Test
    public void test() throws InterruptedException {

        serviceQueueBuilder.setServiceObject(new MyService());
        final ServiceQueue serviceQueue = serviceQueueBuilder.buildAndStartAll();

        IMyService proxy = serviceQueue.createProxyWithAutoFlush(IMyService.class,
                10, TimeUnit.MILLISECONDS);

        final AtomicInteger count = new AtomicInteger();
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        proxy.callCount(new Callback<Integer>() {
            @Override
            public void accept(Integer integer) {
                count.set(integer);
                countDownLatch.countDown();
            }
        });
        countDownLatch.await();

        assertEquals(1, count.get());

        proxy.forceError();
        final CountDownLatch countDownLatch2 = new CountDownLatch(1);

        proxy.callCount(new Callback<Integer>() {
            @Override
            public void accept(Integer integer) {
                count.set(integer);
                countDownLatch2.countDown();
            }
        });

        Sys.sleep(100);

        countDownLatch2.await();


        assertEquals(2, count.get());

        serviceQueue.stop();
    }

    public static interface IMyService {
        void forceError();

        void callCount(Callback<Integer> callback);
    }

    public static class MyService {

        private boolean forceError;
        private int callCount;

        public void forceError() {
            forceError = true;
        }

        @QueueCallback({QueueCallbackType.EMPTY, QueueCallbackType.LIMIT, QueueCallbackType.IDLE})
        public void process() {

            if (forceError) {
                throw new RuntimeException("Exception was thrown on purpose");
            }
        }


        public int callCount() {
            callCount++;
            return callCount;
        }

    }


}
