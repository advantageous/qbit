package io.advantageous.qbit.service;


import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.reactive.*;
import io.advantageous.qbit.util.Timer;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static io.advantageous.qbit.reactive.CountDownAsyncLatch.countDownLatch;

public class RunnableCallbackTest {


    public static void main(String... args) throws Exception {


        final Reactor reactor = new Reactor(Timer.timer(), 10_000, TimeUnit.MILLISECONDS);
        final AtomicBoolean stop = new AtomicBoolean();

        Thread thread = new Thread(() -> {

            while (!stop.get()) {

                Sys.sleep(1);
                reactor.process();
            }
        });

        thread.start();

        final PretendService serviceA = new PretendService("SERVICE A");
        final PretendService serviceB = new PretendService("SERVICE B");
        final PretendService serviceC = new PretendService("SERVICE C");
        final AtomicReference<String> serviceAReturn = new AtomicReference<>();
        final AtomicReference<String> serviceCReturn = new AtomicReference<>();


        final CallbackBuilder callbackBuilder = reactor.callbackBuilder();

        /* Register a coordinator that checks for return values from service A and C */
        final CallbackCoordinator coordinator = reactor.coordinatorBuilder()
                .setCoordinator(
                        () -> {

                                /* If service A and service C are done, then we are done.
                                * Let the client know.
                                */
                            if (serviceAReturn.get() != null && serviceCReturn.get() != null) {
                                sendResponseBackToClient(serviceAReturn.get(), serviceCReturn.get());
                                return true;  //true means we are done
                            }

                            return false;
                        })
                .setTimeOutHandler(() -> {
                    System.out.println("Coordinator timed out");
                    sendTimeoutBackToClient();
                })
                .build();

        final CountDownAsyncLatch latch = countDownLatch(2,
                () -> {
                    System.out.println("From Latch");
                    coordinator.finished();
                }
        );


          /* Create a callbackWithTimeout for service A to demonstrate
            a callbackWithTimeout to show that it can be cancelled. */
        final Callback<String> serviceACallback =
                callbackBuilder
                        .setCallback(String.class, returnValueFromA -> {

                            serviceAReturn.set(returnValueFromA);
                            latch.countDown();
                        })
                        .setOnTimeout(() -> {
                            System.out.println("Service A timed out");
                            sendTimeoutBackToClient();
                            coordinator.cancel();
                        })
                        .setTimeoutDuration(4)
                        .build();

        /* Call service A using the serviceACallback. */
        serviceA.serviceCall(serviceACallback, 1, " from main");

        /* Create service C callback. */
        final Callback<String> serviceCCallback =
                callbackBuilder
                        .setCallback(String.class, returnValueFromC -> {

                            serviceCReturn.set(returnValueFromC);
                            latch.countDown();
                        })
                        .setOnTimeout(() -> {
                            System.out.println("Service C timed out");
                            sendTimeoutBackToClient();
                            coordinator.cancel();
                        })
                        .setTimeoutDuration(4)
                        .build();



        /* Call Service B, register a callback
             which call service C on service b completion. */
        serviceB.serviceCall(
                reactor.callbackBuilder()
                        .setCallback(
                                returnValue ->
                                        serviceC.serviceCall(serviceCCallback, 1,
                                                " from " + returnValue)
                        ).build(),
                1, " from main");


        Sys.sleep(20_000);
        stop.set(true);

        thread.join();

        System.out.println("done");


    }

    private static void sendTimeoutBackToClient() {

        System.out.println("You have timed out");
    }

    private static void sendResponseBackToClient(String a, String ab) {

        System.out.println(a + "::" + ab);

    }

    public static class PretendService {


        private final String name;

        PretendService(final String name) {

            this.name = name;
        }

        public void serviceCall(final Callback<String> callback, final int seconds, String message) {

            Thread thread = new Thread(() -> {
                Sys.sleep(seconds * 1000);
                callback.accept(name + "::" + message);
            });
            thread.start();
        }
    }

}