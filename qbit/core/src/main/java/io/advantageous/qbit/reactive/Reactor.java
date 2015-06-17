package io.advantageous.qbit.reactive;

import io.advantageous.qbit.reactive.impl.AsyncFutureCallbackImpl;
import io.advantageous.qbit.service.ServiceProxyUtils;
import io.advantageous.qbit.util.Timer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * You could use a reactor per service.
 * Right now we don't use QBit queues, but we could. We need a way to flush the queues async.
 * Reactor. We could get a lot perf from refactoring this to use QBit queues instead of BlockingQueue.
 * created by rhightower on 3/22/15.
 */
@SuppressWarnings("UnusedReturnValue")
public class Reactor {

    /* The queues could benefit from being QBit queues. */

    /* Future can come back on another thread so it has to be handled by thread safe queue. */
    private final BlockingQueue<AsyncFutureCallback<?>> futureQueue = new LinkedTransferQueue<>();

    /* Future can come back on another thread so it has to be handled by thread safe queue. */
    private final BlockingQueue<AsyncFutureCallback<?>> removeFutureQueue = new LinkedTransferQueue<>();


    /* Coordinator can come back on another thread so it has to be handled by thread safe queue. */
    private final BlockingQueue<CallbackCoordinator> coordinatorQueue = new LinkedTransferQueue<>();


    /* Coordinator can come back on another thread so it has to be handled by thread safe queue. */
    private final BlockingQueue<CallbackCoordinator> removeCoordinatorQueue = new LinkedTransferQueue<>();


    /** List of futures that we are managing. */
    private final Set<AsyncFutureCallback<?>> futureList = new HashSet<>();


    /** List of coordinators that we are managing. */
    private final Set<CallbackCoordinator> coordinatorList = new HashSet<>();


    /** Timer to keep track of current time. */
    private final Timer timer;

    /** Time out to use if a timeout is not specified. */
    private final long defaultTimeOut;

    /** Current time. */
    private long currentTime;

    /** Keeps list of repeating tasks. */
    private List<RepeatingTask> repeatingTasks = new ArrayList<>(1);


    /** Keeps list of collaborating services to flush. */
    private List<Object> collaboratingServices = new ArrayList<>(1);

    /**
     * Reactor
     * @param timer timer
     * @param defaultTimeOut defaultTimeOut
     * @param timeUnit time unit for default time out
     */
    public Reactor(final Timer timer, long defaultTimeOut, TimeUnit timeUnit) {
        this.timer = timer;
        currentTime = timer.now();
        this.defaultTimeOut = timeUnit.toMillis(defaultTimeOut);
    }



    /** Add an object that is auto flushed.
     *
     * @param serviceObject as service object that will be auto-flushed.
     */
    public void addServiceToFlush(final Object serviceObject) {
        collaboratingServices.add(serviceObject);
    }

    /** A repeating task. */
    class RepeatingTask {
        private long lastTimeInvoked;
        private final Runnable task;
        private final long repeatEveryMS;


        public RepeatingTask(Runnable task, TimeUnit timeUnit, long repeatEvery) {
            this.task = task;
            this.repeatEveryMS = timeUnit.toMillis(repeatEvery);
        }
    }

    /** Add a task that gets repeated. */
    public void addRepeatingTask(final long repeatEvery, final TimeUnit timeUnit, final Runnable task) {

        repeatingTasks.add(new RepeatingTask( task, timeUnit, repeatEvery ));
    }

    /** Process items in reactor. */
    public void process() {

        /** Manages lists of coordinators, and callbacks which can come back async. */
        drainQueues();

        currentTime = timer.now();

        /* Check to see if callbacks completed or timed out. */
        monitorCallBacks();

        /* Check to see if coordinators completed or timed out. */
        monitorCallbackCoordinators();

        /* flush services. */
        collaboratingServices.forEach(ServiceProxyUtils::flushServiceProxy);

        processRepeatingTasks();
    }

    public void processRepeatingTasks() {

        /* Run repeating tasks if needed. */
        repeatingTasks.forEach(repeatingTask -> {
            if (currentTime - repeatingTask.lastTimeInvoked > repeatingTask.repeatEveryMS){
                repeatingTask.lastTimeInvoked = currentTime;
                repeatingTask.task.run();
            }
        });
    }


    @SuppressWarnings({"UnusedReturnValue", "SameReturnValue"})
    private boolean drainQueues() {

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

        AsyncFutureCallback<?> futureCallback = futureQueue.poll();
        while (futureCallback != null) {
            futureList.add(futureCallback);
            futureCallback = futureQueue.poll();
        }

        futureCallback = removeFutureQueue.poll();
        while (futureCallback != null) {
            futureList.remove(futureCallback);
            futureCallback = futureQueue.poll();
        }


        return false;
    }

    public CallbackBuilder callbackBuilder() {
        return CallbackBuilder.callbackBuilder(this);
    }

    public CoordinatorBuilder coordinatorBuilder() {
        return CoordinatorBuilder.coordinatorBuilder(this);
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

    public <T> AsyncFutureCallback<T> callbackWithTimeoutAndErrorHandler(
            final Callback<T> callback,
            final long timeoutDuration,
            final TimeUnit timeUnit,
            final Consumer<Throwable> onError) {

        return callbackWithTimeoutAndErrorHandlerAndOnTimeout(callback, timeoutDuration, timeUnit, null, onError);

    }


    public <T> AsyncFutureCallback<T> callbackWithTimeoutAndErrorHandlerAndOnTimeout(
            final Callback<T> callback,
            final long timeoutDuration,
            final TimeUnit timeUnit,
            final Runnable onTimeout,
            final Consumer<Throwable> onError) {

        final AtomicReference<AsyncFutureCallback<T>> ref = new AtomicReference<>();

        final AsyncFutureCallbackImpl<T> asyncFutureCallback =
                AsyncFutureCallbackImpl.callback(callback, currentTime,
                        timeUnit.toMillis(timeoutDuration),
                        createOnFinished(ref)
                        , onTimeout, onError);

        ref.set(asyncFutureCallback);
        addCallback(asyncFutureCallback);
        return asyncFutureCallback;

    }

    public <T> void addCallback(final AsyncFutureCallbackImpl<T> asyncFutureCallback) {
        futureQueue.add(asyncFutureCallback);
    }

    private <T> Runnable createOnFinished(final AtomicReference<AsyncFutureCallback<T>> ref) {
        return () -> Reactor.this.removeFuture(ref.get());
    }


    public <T> AsyncFutureCallback<T> callbackWithTimeout(Class<T> cls, final Callback<T> callback,
                                                          final long timeoutDuration,
                                                          final TimeUnit timeUnit) {


        return callbackWithTimeout(callback, timeoutDuration, timeUnit);
    }


    public <T> AsyncFutureCallback<T> callbackWithTimeoutAndErrorHandler(Class<T> cls,
                                                                         final Callback<T> callback,
                                                                         final long timeoutDuration,
                                                                         final TimeUnit timeUnit,
                                                                         final Consumer<Throwable> onError) {


        return callbackWithTimeoutAndErrorHandler(callback, timeoutDuration, timeUnit, onError);
    }

    public CallbackCoordinator coordinate(final CallbackCoordinator coordinator) {

        this.coordinatorQueue.add(coordinator);
        return coordinator;
    }


    @SuppressWarnings("UnusedReturnValue")
    public CallbackCoordinator removeCoordinator(final CallbackCoordinator coordinator) {

        this.removeCoordinatorQueue.add(coordinator);
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
                                                     final Runnable timeOutHandler,
                                                     final Runnable finishedHandler) {

        final long timeoutDurationMS = timeUnit.toMillis(timeoutDuration);

        final long theStartTime = startTime == -1 ? currentTime : startTime;


        final CallbackCoordinator wrapper = new CallbackCoordinator() {

            AtomicBoolean done = new AtomicBoolean();

            @Override
            public boolean checkComplete() {
                if (done.get()) {
                    return true;
                }

                if (coordinator.checkComplete()) {
                    done.set(true);
                }

                return done.get();

            }


            public boolean timedOut(long now) {


                if (startTime() == -1 || timeOutDuration() == -1) {
                    return false;
                }
                if ((now - startTime()) > timeOutDuration()) {

                    if (!done.get()) {
                        timeOutHandler.run();
                        done.set(true);
                    }
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public long timeOutDuration() {
                return coordinator.timeOutDuration() == -1 ? timeoutDurationMS : coordinator.timeOutDuration();
            }

            @Override
            public long startTime() {
                return coordinator.startTime() == -1 ? theStartTime : coordinator.startTime();
            }

            public void finished() {
                if (checkComplete()) {
                    removeCoordinator(this);
                }
                if (finishedHandler!=null) {
                    finishedHandler.run();
                }
                coordinator.finished();
            }

            public void cancel() {
                done.set(true);
                removeCoordinator(this);
                coordinator.cancel();
            }
        };


        this.coordinatorQueue.add(wrapper);
        return wrapper;
    }

    private void monitorCallBacks() {

        final List<AsyncFutureCallback<?>> removeList = new ArrayList<>();

        long now = currentTime;
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
                callable.finished();
                removeList.add(callable);
            } else if (callable.timedOut(currentTime)) {
                removeList.add(callable);
            }
        }
        coordinatorList.removeAll(removeList);

    }

}
