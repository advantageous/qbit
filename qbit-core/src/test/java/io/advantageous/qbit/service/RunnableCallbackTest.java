package io.advantageous.qbit.service;


import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.util.Timer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class RunnableCallbackTest {


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


    public static class Reactor {


        private final BlockingQueue<AsyncFutureCallback<?>> futureQueue = new LinkedTransferQueue<>();
        private final BlockingQueue<CallbackCoordinator> coordinatorQueue = new LinkedTransferQueue<>();
        private final BlockingQueue<CallbackCoordinator> removeCoordinatorQueue = new LinkedTransferQueue<>();

        private final List<AsyncFutureCallback<?>> futureList = new ArrayList<>();
        private final List<CallbackCoordinator> coordinatorList = new ArrayList<>();
        private final Timer timer;

        private long currentTime;

        public Reactor(final Timer timer) {

            this.timer = timer;
            currentTime = timer.now();
        }

        public void process() {


            drainQueues();
            currentTime = timer.now();
            monitorCallBacks();
            monitorCallbackCoordinators();
        }


        private boolean drainQueues() {
            AsyncFutureCallback<?> futureCallback = futureQueue.poll();
            while (futureCallback != null) {
                futureList.add(futureCallback);
                futureCallback = futureQueue.poll();
            }

            CallbackCoordinator callable = coordinatorQueue.poll();
            while (callable != null) {
                coordinatorList.add(callable);
                callable = coordinatorQueue.poll();
            }

            callable = removeCoordinatorQueue.poll();
            while (callable != null) {
                coordinatorList.remove(callable);
                callable = coordinatorQueue.poll();
            }

            return false;
        }

        public <T> Callback<T> callback(final Callback<T> callback) {

            final AsyncFutureCallback<T> asyncFutureCallback =
                    AsyncFutureCallback.callback(callback, Timer.timer().now(), 4000);
            futureQueue.add(asyncFutureCallback);
            return asyncFutureCallback;

        }


        public <T> AsyncFutureCallback<T> callback(Class<T> cls, final Callback<T> callback) {

            final AsyncFutureCallback<T> asyncFutureCallback =
                    AsyncFutureCallback.callback(callback, Timer.timer().now(), 4000);
            futureQueue.add(asyncFutureCallback);
            return asyncFutureCallback;

        }

        public CallbackCoordinator coordinate(final CallbackCoordinator coordinator) {

            this.coordinatorQueue.add(coordinator);
            return coordinator;
        }


        public CallbackCoordinator removeCoordinator(final CallbackCoordinator coordinator) {

            this.removeCoordinatorQueue.add(new CallbackCoordinator() {
                @Override
                public boolean checkComplete() {
                    return coordinator.checkComplete();
                }


                public void finished() {
                    removeCoordinator(this);
                }

            });
            return coordinator;
        }


        public CallbackCoordinator coordinateWithTimeout(final CallbackCoordinator coordinator,
                                                         final long startTime,
                                                         final long timeoutDuration,
                                                         final TimeUnit timeUnit,
                                                         final Runnable timeOutHandler) {

            final long timeoutDurationMS = timeUnit.toMillis(timeoutDuration);
            this.coordinatorQueue.add(
                    new CallbackCoordinator() {
                        @Override
                        public boolean checkComplete() {
                            return coordinator.checkComplete();
                        }


                        public boolean timedOut(long now) {

                            if (startTime() == -1 || timeOutDuration() == -1) {
                                return false;
                            }
                            if ((now - startTime()) > timeOutDuration() ) {

                                timeOutHandler.run();
                                return true;
                            } else {
                                return false;
                            }
                        }

                        @Override
                        public long timeOutDuration() {
                            return timeoutDurationMS;
                        }

                        @Override
                        public long startTime() {
                            return startTime;
                        }

                        public void finished() {
                            if (checkComplete()) {
                                removeCoordinator(this);
                            }
                        }
                    }
            );
            return coordinator;
        }

        private void monitorCallBacks() {

            final List<AsyncFutureCallback<?>> removeList = new ArrayList<>();

            long now = Timer.timer().now();
            for (AsyncFutureCallback<?> callback : futureList) {
                if (callback.isDone()) {
                    callback.run();
                    removeList.add(callback);
                } else {
                    if (callback.checkTimeOut(now)) {
                        removeList.add(callback);
                    }
                }
            }
            futureList.removeAll(removeList);
        }


        private void monitorCallbackCoordinators() {


            final List<CallbackCoordinator> removeList = new ArrayList<>();

            for (CallbackCoordinator callable : coordinatorList) {
                if (callable.checkComplete()) {
                    removeList.add(callable);
                } else if (callable.timedOut(currentTime)) {
                    removeList.add(callable);
                }
            }
            coordinatorList.removeAll(removeList);

        }

    }




    public static void handleReturnFromC(AtomicReference<String> serviceAReturn,
                                         CallbackCoordinator coordinator,
                                         Reactor reactor) {

        if (serviceAReturn.get()!=null) {
           reactor.removeCoordinator(coordinator);
        }

    }

    public static void main(String... args) throws Exception {


        final Reactor reactor = new Reactor(Timer.timer());
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


        /* Create a callback for service A to demonstrate
            a callback to show that it can be cancelled. */
        final AsyncFutureCallback<String> serviceACallback =
                reactor.callback(String.class, serviceAReturn::set);

        /* Call service A using the callback. */
        serviceA.serviceCall(serviceACallback, 1, " from main");


        /* Register a coordinator that checks for return values from service A and C */
        CallbackCoordinator coordinator = reactor.coordinateWithTimeout(() -> {

            /* If service A and service C are done, then we are done.
            * Let the client know.
            */
            if (serviceAReturn.get() != null && serviceCReturn.get() != null) {
                sendResponseBackToClient(serviceAReturn.get(), serviceCReturn.get());
                return true;  //true means we are done
            }

            return false;

        }, Timer.timer().now(), 2, TimeUnit.SECONDS, () -> sendTimeoutBackToClient());


        /* Call Service B, register a callback
             which call service C on service b completion. */
        serviceB.serviceCall(
                reactor.callback(returnValue ->
                        serviceC.serviceCall(reactor.callback(
                                returnValueFromC -> {
                                    serviceCReturn.set(returnValueFromC);
                                    handleReturnFromC(serviceAReturn, coordinator, reactor);
                                }
                        ), 2, " from " + returnValue)
                ), 2, " from main");



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

}