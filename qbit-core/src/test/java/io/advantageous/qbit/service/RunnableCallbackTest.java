package io.advantageous.qbit.service;


import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.util.Timer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static io.advantageous.boon.Boon.puts;

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


    public static class Reactor implements Runnable{


        final BlockingQueue<AsyncFutureCallback<?>> queue = new ArrayBlockingQueue<>(100);

        final BlockingQueue<CallbackCoordinator> callQueue = new ArrayBlockingQueue<>(100);
        final List<AsyncFutureCallback<?>> callbackList = new ArrayList<>();
        final List<CallbackCoordinator> callableList = new ArrayList<>();

        final Thread thread;
        final AtomicBoolean stopped = new AtomicBoolean();
        Reactor (){
            this.thread = new Thread(this);
            this.thread.start();
        }

        @Override
        public void run() {

            while (!stopped.get()) {

                if (drainQueues()) break;

                monitorCallBacks();

                monitorCallable();
            }
        }


        private boolean drainQueues() {
            try {
                AsyncFutureCallback<?> value = queue.poll(1, TimeUnit.SECONDS);
                while (value !=null) {
                    callbackList.add(value);
                    value = queue.poll();
                }
            } catch (InterruptedException e) {
                return true;
            }

            CallbackCoordinator callable = callQueue.poll();
            while (callable !=null) {
                callableList.add(callable);
                callable = callQueue.poll();
            }
            return false;
        }

        public <T> Callback<T> callback(final Callback<T> callback) {

            final AsyncFutureCallback<T> asyncFutureCallback =
                    AsyncFutureCallback.callback(callback, Timer.timer().now(), 4000);
            queue.add(asyncFutureCallback);
            return asyncFutureCallback;

        }


        public <T> AsyncFutureCallback<T> callback(Class<T> cls, final Callback<T> callback) {

            final AsyncFutureCallback<T> asyncFutureCallback =
                    AsyncFutureCallback.callback(callback, Timer.timer().now(), 4000);
            queue.add(asyncFutureCallback);
            return asyncFutureCallback;

        }

        public CallbackCoordinator coordinate(final CallbackCoordinator coordinator) {

            this.callQueue.add(coordinator);
            return coordinator;
        }

        private void monitorCallBacks() {

            final List<AsyncFutureCallback<?>> removeList = new ArrayList<>();

            long now = Timer.timer().now();
            for (AsyncFutureCallback<?> callback : callbackList) {
                if (callback.isDone()) {
                    callback.run();
                    removeList.add(callback);
                } else {
                    if (callback.checkTimeOut(now)) {
                        removeList.add(callback);
                    }
                }
            }
            callbackList.removeAll(removeList);
        }


        private void monitorCallable() {

            final List<CallbackCoordinator> removeList = new ArrayList<>();

            for (CallbackCoordinator callable : callableList) {
                try {
                    if (callable.checkComplete()) {
                        removeList.add(callable);
                    }
                } catch (Exception e) {


                }
            }
            callableList.removeAll(removeList);

        }

        public void stop() {
            stopped.set(true);
        }
    }





    public static void main(String... args) throws Exception {


        Reactor reactor = new Reactor();

        final PretendService serviceA = new PretendService("SERVICE A");
        final PretendService serviceB = new PretendService("SERVICE B");
        final PretendService serviceC = new PretendService("SERVICE C");
        final AtomicReference<String> serviceAReturn = new AtomicReference<>();
        final AtomicReference<String> serviceCReturn = new AtomicReference<>();

        final long startTime = Timer.timer().now();

        /* Create a callback for service A to demonstrate
            a callback to show that it can be cancelled. */
        final AsyncFutureCallback<String> serviceACallback =
                reactor.callback(String.class, serviceAReturn::set);

        /* Call service A using the callback. */
        serviceA.serviceCall(serviceACallback, 1, " from main");

        /* Call Service B, register a callback
             which call service C on service b completion. */
        serviceB.serviceCall(
                reactor.callback(returnValue ->
                        serviceC.serviceCall(reactor.callback(serviceCReturn::set), 1, " from " + returnValue)
                ), 1, " from main");


        /* Register a coordinator that checks for return values form service A and C */
        reactor.coordinate(() -> {

            /* If service A and service C are done, then we are done.
            * Let the client know.
            */
            if (serviceAReturn.get()!=null && serviceCReturn.get()!=null) {
                sendResponseBackToClient(serviceAReturn.get(), serviceCReturn.get());
                return true;  //true means we are done
            }

            /* We are not done, so check to see if 4s elapsed, then signal we are done.*/
            if ( Timer.timer().now() - startTime > 4000) {
                sendTimeoutBackToClient();
                serviceACallback.cancel(true);
                return true; //true means we are done
            }
            return false; //keep going
        });


        Sys.sleep(20_000);
        reactor.stop();

        puts("DONE");



    }

    private static void sendTimeoutBackToClient() {
        System.out.println("You have timed out");
    }

    private static void sendResponseBackToClient(String a, String ab) {

        System.out.println(a + "::" + ab);

    }

}