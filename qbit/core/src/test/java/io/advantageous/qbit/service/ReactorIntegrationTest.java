package io.advantageous.qbit.service;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.annotation.QueueCallback;
import io.advantageous.qbit.annotation.QueueCallbackType;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.reactive.Reactor;
import io.advantageous.qbit.reactive.ReactorBuilder;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ReactorIntegrationTest {

    private static final AtomicBoolean timeout = new AtomicBoolean();

    @Test
    public void test() {

        final SomeServiceA someServiceA = new SomeServiceA();
        final ServiceQueue serviceQueueA = ServiceBuilder.serviceBuilder()
                .setServiceObject(someServiceA).buildAndStartAll();
        final ISomeService someServiceAProxy = serviceQueueA
                .createProxyWithAutoFlush(ISomeService.class, 50, TimeUnit.MILLISECONDS);

        final Reactor reactor = ReactorBuilder.reactorBuilder().build();
        final SomeServiceB someServiceB = new SomeServiceB(someServiceAProxy, reactor);


        final ServiceQueue serviceQueueB = ServiceBuilder.serviceBuilder()
                .setServiceObject(someServiceB).buildAndStartAll();
        final ISomeService someServiceBProxy = serviceQueueB
                .createProxyWithAutoFlush(ISomeService.class, 50, TimeUnit.MILLISECONDS);

        someServiceBProxy.message(s -> System.out.println("PRINTLN " + s));

        Sys.sleep(5_000);

        assertFalse(timeout.get());

        final AtomicInteger count = new AtomicInteger();
        for (int index = 0; index < 1000; index++) {
            Sys.sleep(1);
            someServiceBProxy.message(s -> count.incrementAndGet());
        }


        Sys.sleep(1_000);

        assertEquals(1000, count.get());
    }

    public static interface ISomeService {
        void message(Callback<String> callback);
    }

    public static class SomeServiceA {
        public void message(Callback<String> callback) {
            callback.accept("SomeServiceA");
        }
    }

    public static class SomeServiceB {

        private final Reactor reactor;
        private final ISomeService someService;

        public SomeServiceB(final ISomeService someService,
                            final Reactor reactor) {
            this.someService = someService;
            this.reactor = reactor;

            reactor.addServiceToFlush(someService);
        }

        public void message(final Callback<String> callback) {
            Callback<String> callback2 = reactor.callbackBuilder().setCallback(String.class, o -> {
                callback.accept("accept " + o);

            }).setOnTimeout(() -> {

                timeout.set(true);
                throw new IllegalStateException("PROBLEM");
            }).setOnError(throwable -> {
                throwable.printStackTrace();
            }).
                    build();

            someService.message(callback2);
        }

        @QueueCallback({QueueCallbackType.LIMIT,
                QueueCallbackType.EMPTY,
                QueueCallbackType.IDLE})
        public void process() {

            //System.out.println("PROCESS");
            reactor.process();
        }
    }


}
