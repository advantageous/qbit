package io.advantageous.qbit.example.service;

import io.advantageous.qbit.QBit;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.service.ServiceBuilder;
import io.advantageous.qbit.service.ServiceProxyUtils;
import io.advantageous.qbit.service.ServiceQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static io.advantageous.boon.core.IO.puts;

public class ServiceMain {

    public interface EchoService {
        void echo(final Callback<String> callback, String arg);
    }

    public static class EchoServiceImpl implements EchoService {

        @Override
        public void echo(final Callback<String> callback, final String arg) {
            callback.returnThis(arg);
        }
    }


    public static void main(final String... args) throws Exception {

        final ServiceQueue serviceQueue = ServiceBuilder.serviceBuilder()
                .setServiceObject(new EchoServiceImpl()).build().startAll();


        final int numThreads = 10;
        final int numCalls = 100_000;
        final List<Thread> threadList = new ArrayList<>();

        for (int t = 0; t < numThreads; t++) {
            final EchoService echoService = serviceQueue.createProxy(EchoService.class);
            final String rootUniqueId = UUID.randomUUID().toString();
            final CountDownLatch latch = new CountDownLatch(numCalls);

            threadList.add(new Thread(() -> {

                for (int index = 0; index< numCalls; index++) {
                    final String compareTo = rootUniqueId + "-" + index;
                    final int callCount = index;
                    echoService.echo(response -> {

                        if (!compareTo.equals(response)) {
                            puts(compareTo, response);
                        }

                        if (callCount%1000==0) puts(Thread.currentThread().getName(), callCount, response, compareTo);

                        latch.countDown();
                    }, compareTo);
                }

                ServiceProxyUtils.flushServiceProxy(echoService);

                try {
                    latch.await(100, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                puts("THIS MANY CALLS LEFT ", latch.getCount());

            }));



        }

        threadList.forEach(Thread::start);

        threadList.forEach((thread) -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });


        puts("DONE ");

        serviceQueue.stop();

        QBit.factory().shutdownSystemEventBus();

    }

}
