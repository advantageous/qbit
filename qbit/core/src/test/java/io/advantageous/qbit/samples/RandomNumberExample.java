package io.advantageous.qbit.samples;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.reactive.AsyncFutureCallback;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.reactive.CallbackBuilder;
import io.advantageous.qbit.service.ServiceBuilder;
import io.advantageous.qbit.service.ServiceProxyUtils;
import io.advantageous.qbit.service.ServiceQueue;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class RandomNumberExample {


    public static void main(String... args) throws Exception {

        final ServiceQueue serviceQueue = ServiceBuilder.serviceBuilder().setServiceObject(randomNumberService()).buildAndStartAll();

        final RandomNumberServiceAsync randomNumberServiceAsync =
                serviceQueue.createProxyWithAutoFlush(RandomNumberServiceAsync.class, 5, TimeUnit.SECONDS);


        for (int a = 0; a < 100; a++) {

            final AsyncFutureCallback<Integer> callback = CallbackBuilder.newCallbackBuilder()
                    .withCallback(l -> System.out.println("" + l))
                    .withErrorHandler(RandomNumberExample::handleError)
                    .<Integer>build();
            randomNumberServiceAsync.getRandom(callback, 100, a);
        }

        ServiceProxyUtils.flushServiceProxy(randomNumberServiceAsync);

        Sys.sleep(1000);


        serviceQueue.stop();
        QBit.factory().shutdownSystemEventBus();

    }


    private static void handleError(Throwable e) {
        System.out.println("blew up" + e);
    }


    public static RandomNumberService randomNumberService() {
        return (max, min) -> {
            final Random random = new Random();

            if (String.valueOf(random.nextFloat()).endsWith("7"))
                throw new RuntimeException("aaaaaahhhhh");

            return random.nextInt(max - min + 1) + min;
        };
    }

    private interface RandomNumberService {
        int getRandom(int max, int min);
    }

    private interface RandomNumberServiceAsync {
        void getRandom(Callback<Integer> callback, int max, int min);
    }
}
