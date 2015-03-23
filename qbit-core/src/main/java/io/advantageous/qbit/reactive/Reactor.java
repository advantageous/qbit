package io.advantageous.qbit.reactive;

import io.advantageous.qbit.reactive.impl.AsyncFutureCallbackImpl;
import io.advantageous.qbit.util.Timer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Reactor
* Created by rhightower on 3/22/15.
*/
public class Reactor {


    private final BlockingQueue<AsyncFutureCallback<?>> futureQueue = new LinkedTransferQueue<>();
    private final BlockingQueue<AsyncFutureCallback<?>> removeFutureQueue = new LinkedTransferQueue<>();

    private final BlockingQueue<CallbackCoordinator> coordinatorQueue = new LinkedTransferQueue<>();
    private final BlockingQueue<CallbackCoordinator> removeCoordinatorQueue = new LinkedTransferQueue<>();

    private final List<AsyncFutureCallback<?>> futureList = new ArrayList<>();
    private final List<CallbackCoordinator> coordinatorList = new ArrayList<>();
    private final Timer timer;
    private final long defaultTimeOut;

    private long currentTime;

    public Reactor(final Timer timer, long defaultTimeOut, TimeUnit timeUnit) {

        this.timer = timer;
        currentTime = timer.now();
        this.defaultTimeOut = timeUnit.toMillis(defaultTimeOut);
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

        futureCallback  = removeFutureQueue.poll();
        while (futureCallback != null) {
            futureList.remove(futureCallback);
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

    public <T> AsyncFutureCallback<T> callback(final Callback<T> callback) {

        return callbackWithTimeout(callback, defaultTimeOut, TimeUnit.MILLISECONDS);
    }


    public <T> AsyncFutureCallback<T> callback(Class<T> cls, final Callback<T> callback) {

        return callbackWithTimeout(callback, defaultTimeOut, TimeUnit.MILLISECONDS);
    }


    public <T> AsyncFutureCallback<T> callbackWithTimeout(final Callback<T> callback,
                                                          final long timeoutDuration,
                                                          final TimeUnit timeUnit) {

        return callbackWithTimeoutAndErrorHandler(callback, timeoutDuration, timeUnit, null);

    }

    public <T> AsyncFutureCallback<T> callbackWithTimeoutAndErrorHandler(final Callback<T> callback,
                                                          final long timeoutDuration,
                                                          final TimeUnit timeUnit,
                                                          final Consumer<Throwable> onError) {

        final AtomicReference<AsyncFutureCallback<T>> ref = new AtomicReference<>();

        final AsyncFutureCallbackImpl<T> asyncFutureCallback =
                AsyncFutureCallbackImpl.callback(callback, Timer.timer().now(),
                        timeUnit.toMillis(timeoutDuration),
                        () -> Reactor.this.removeFuture(ref.get()), onError);

        ref.set(asyncFutureCallback);
        futureQueue.add(asyncFutureCallback);
        return asyncFutureCallback;

    }



    public <T> AsyncFutureCallback<T> callbackWithTimeout(Class<T> cls, final Callback<T> callback,
                                                          final long timeoutDuration,
                                                          final TimeUnit timeUnit) {


        return callbackWithTimeout(callback, timeoutDuration, timeUnit);
    }


    public <T> AsyncFutureCallback<T> callbackWithTimeoutAndErrorHandler(Class<T> cls, final Callback<T> callback,
                                                          final long timeoutDuration,
                                                          final TimeUnit timeUnit,
                                                          final Consumer<Throwable> onError) {


        return callbackWithTimeoutAndErrorHandler(callback, timeoutDuration, timeUnit, onError);
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


    public <T> AsyncFutureCallback<T> removeFuture(AsyncFutureCallback<T> asyncFutureCallback) {

        this.removeFutureQueue.offer(asyncFutureCallback);
        return asyncFutureCallback;
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
