package io.advantageous.qbit.samples;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.reactive.AsyncFutureCallback;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.reactive.CallbackBuilder;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.service.ServiceBundleBuilder;

import java.util.Random;

import static io.advantageous.qbit.service.ServiceProxyUtils.flushServiceProxy;

public class RandomNumberExampleUsingServiceBundle {

    public static void main(String... args) throws Exception {

        final ServiceBundle serviceBundle = ServiceBundleBuilder.serviceBundleBuilder().build();

        serviceBundle.addServiceObject("RandomNumberService", randomNumberService());

        serviceBundle.start();

        final RandomNumberServiceAsync randomNumberServiceAsync =
                serviceBundle.createLocalProxy(RandomNumberServiceAsync.class, "RandomNumberService");


        for (int a = 0; a < 100; a++) {

            final AsyncFutureCallback<Integer> callback = CallbackBuilder.newCallbackBuilder()
                    .withCallback(
                            l -> System.out.println("" + l)
                    )
                    .withErrorHandler(
                            e -> handleError(e)
                    )
                    .<Integer>build();
            randomNumberServiceAsync.getRandom(callback, 100, a);
        }

        flushServiceProxy(randomNumberServiceAsync);

        Sys.sleep(1_000);
        serviceBundle.stop();
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
