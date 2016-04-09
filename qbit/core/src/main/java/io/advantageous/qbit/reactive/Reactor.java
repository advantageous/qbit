package io.advantageous.qbit.reactive;

import io.advantageous.qbit.reactive.async.AsyncFutureBlockOnGetCallbackImpl;
import io.advantageous.qbit.reactive.async.AsyncFutureCallbackImpl;
import io.advantageous.qbit.service.ServiceProxyUtils;
import io.advantageous.qbit.time.Duration;
import io.advantageous.qbit.util.Timer;
import org.slf4j.Logger;

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
import java.util.stream.Collectors;

/**
 * You could use a reactor per service.
 * Reactor ensures that callbacks execute on thread of caller not callee for thread safety.
 * <p>
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


    /**
     * List of futures that we are managing.
     */
    private final Set<AsyncFutureCallback<?>> futureList = new HashSet<>();


    /**
     * List of coordinators that we are managing.
     */
    private final Set<CallbackCoordinator> coordinatorList = new HashSet<>();


    /**
     * Timer to keep track of current time.
     */
    private final Timer timer;

    /**
     * Time out to use if a timeout is not specified.
     */
    private final long defaultTimeOut;

    /**
     * Current time.
     */
    private long currentTime;

    /**
     * Keeps list of repeating tasks.
     */
    private List<RepeatingTask> repeatingTasks = new ArrayList<>(1);


    private List<FireOnceTask> fireOnceAfterTasks = new ArrayList<>(1);


    /**
     * Keeps list of collaborating services to flush.
     */
    private List<Object> collaboratingServices = new ArrayList<>(1);

    /**
     * Reactor
     *
     * @param timer          timer
     * @param defaultTimeOut defaultTimeOut
     * @param timeUnit       time unit for default time out
     */
    public Reactor(final Timer timer, long defaultTimeOut, TimeUnit timeUnit) {
        this.timer = timer;
        currentTime = timer.now();
        this.defaultTimeOut = timeUnit.toMillis(defaultTimeOut);
    }


    /**
     * Add an object that is auto flushed.
     *
     * @param serviceObject as service object that will be auto-flushed.
     */
    public void addServiceToFlush(final Object serviceObject) {
        collaboratingServices.add(serviceObject);
    }

    /**
     * Add a task that gets repeated.
     *
     * @param repeatEvery repeat Every time period
     * @param timeUnit    unit for repeatEvery
     * @param task        task to perform
     */
    public void addRepeatingTask(final long repeatEvery, final TimeUnit timeUnit, final Runnable task) {

        repeatingTasks.add(new RepeatingTask(task, timeUnit, repeatEvery));
    }


    /**
     * Add a task that gets executed once.
     *
     * @param fireAfter run task after time period
     * @param timeUnit  unit for fireAfter
     * @param task      task to perform
     */
    public void addOneShotAfterTask(final long fireAfter, final TimeUnit timeUnit, final Runnable task) {

        fireOnceAfterTasks.add(new FireOnceTask(task, timeUnit, fireAfter));
    }


    /**
     * Add a task that gets executed once.
     *
     * @param task task to perform
     */
    public void addOneShotTask(final Runnable task) {

        fireOnceAfterTasks.add(new FireOnceTask(task, TimeUnit.MILLISECONDS, 0));
    }

    /**
     * Add a task that gets repeated.
     *
     * @param repeatEvery repeat Every time period
     * @param task        task to perform
     */
    public void addRepeatingTask(final Duration repeatEvery, final Runnable task) {

        repeatingTasks.add(new RepeatingTask(task, repeatEvery.getTimeUnit(), repeatEvery.getDuration()));
    }

    /**
     * Process items in reactor.
     */
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
        processFireOnceTasks();
    }

    public void processRepeatingTasks() {

        /* Run repeating tasks if needed. */
        repeatingTasks.forEach(repeatingTask -> {
            if (currentTime - repeatingTask.lastTimeInvoked > repeatingTask.repeatEveryMS) {
                repeatingTask.lastTimeInvoked = currentTime;
                repeatingTask.task.run();
            }
        });
    }


    public void processFireOnceTasks() {

        /* Run repeating tasks if needed. */
        final List<FireOnceTask> fireOnceTasks = fireOnceAfterTasks.stream()
                .filter(fireOnceTask -> currentTime - fireOnceTask.created > fireOnceTask.fireAfterMS)
                .collect(Collectors.toList());

        fireOnceTasks.forEach(fireOnceTask -> fireOnceTask.task.run());

        fireOnceAfterTasks.removeAll(fireOnceTasks);

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
        return CallbackBuilder.newCallbackBuilderWithReactor(this);
    }

    public CoordinatorBuilder coordinatorBuilder() {
        return CoordinatorBuilder.coordinatorBuilder(this);
    }

    public <T> AsyncFutureCallback<T> callback(final Callback<T> callback) {

        return callbackWithTimeout(callback, defaultTimeOut, TimeUnit.MILLISECONDS);
    }

    public <T> AsyncFutureCallback<T> callbackWithLatch(final Callback<T> callback) {

        return callbackWithTimeoutAndLatch(callback, defaultTimeOut, TimeUnit.MILLISECONDS);
    }

    public <T> AsyncFutureCallback<T> callback(Class<T> cls, final Callback<T> callback) {

        return callbackWithTimeout(callback, defaultTimeOut, TimeUnit.MILLISECONDS);
    }

    public <T> AsyncFutureCallback<T> callbackWithTimeout(final Callback<T> callback,
                                                          final long timeoutDuration,
                                                          final TimeUnit timeUnit) {

        return callbackWithTimeoutAndErrorHandler(callback, timeoutDuration, timeUnit, null);

    }

    public <T> AsyncFutureCallback<T> callbackWithTimeoutAndLatch(final Callback<T> callback,
                                                                  final long timeoutDuration,
                                                                  final TimeUnit timeUnit) {

        return callbackWithTimeoutAndErrorHandlerAndLatch(callback, timeoutDuration, timeUnit, null);

    }

    public <T> AsyncFutureCallback<T> callbackWithTimeoutAndErrorHandler(
            final Callback<T> callback,
            final long timeoutDuration,
            final TimeUnit timeUnit,
            final Consumer<Throwable> onError) {

        return callbackWithTimeoutAndErrorHandlerAndOnTimeout(callback, timeoutDuration, timeUnit, null, onError);

    }

    public <T> AsyncFutureCallback<T> callbackWithTimeoutAndErrorHandlerAndLatch(
            final Callback<T> callback,
            final long timeoutDuration,
            final TimeUnit timeUnit,
            final Consumer<Throwable> onError) {

        return callbackWithTimeoutAndErrorHandlerAndOnTimeoutWithLatch(callback, timeoutDuration, timeUnit, null, onError);

    }

    /**
     * Create a callback
     *
     * @param callback        callback
     * @param timeoutDuration timeout duration
     * @param timeUnit        time unit of timeout
     * @param onTimeout       onTimeout handler
     * @param onError         on error handler
     * @param <T>             T
     * @return callback
     */
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

    /**
     * Create a callback
     *
     * @param callback        callback
     * @param timeoutDuration timeout duration
     * @param timeUnit        time unit of timeout
     * @param onTimeout       onTimeout handler
     * @param onError         on error handler
     * @param <T>             T
     * @return callback
     */
    public <T> AsyncFutureCallback<T> callbackWithTimeoutAndErrorHandlerAndOnTimeoutWithLatch(
            final Callback<T> callback,
            final long timeoutDuration,
            final TimeUnit timeUnit,
            final Runnable onTimeout,
            final Consumer<Throwable> onError) {

        final AtomicReference<AsyncFutureCallback<T>> ref = new AtomicReference<>();

        final AsyncFutureBlockOnGetCallbackImpl<T> asyncFutureCallback =
                AsyncFutureBlockOnGetCallbackImpl.callback(callback, currentTime,
                        timeUnit.toMillis(timeoutDuration),
                        createOnFinished(ref)
                        , onTimeout, onError);

        ref.set(asyncFutureCallback);
        addCallback(asyncFutureCallback);
        return asyncFutureCallback;

    }

    /**
     * Add a callback to manage
     *
     * @param asyncFutureCallback callback
     * @param <T>                 T
     */
    public <T> void addCallback(final AsyncFutureCallback<T> asyncFutureCallback) {
        futureQueue.add(asyncFutureCallback);
    }

    private <T> Runnable createOnFinished(final AtomicReference<AsyncFutureCallback<T>> ref) {
        return () -> Reactor.this.removeFuture(ref.get());
    }

    /**
     * Create a callback
     *
     * @param cls             cls
     * @param callback        callback
     * @param timeoutDuration timeout duration
     * @param timeUnit        timeUnit for timeout
     * @param <T>             T
     * @return callback
     */
    public <T> AsyncFutureCallback<T> callbackWithTimeout(Class<T> cls, final Callback<T> callback,
                                                          final long timeoutDuration,
                                                          final TimeUnit timeUnit) {


        return callbackWithTimeout(callback, timeoutDuration, timeUnit);
    }

    /**
     * Create a callback
     *
     * @param cls             cls
     * @param callback        callback
     * @param timeoutDuration timeout duration
     * @param timeUnit        timeUnit for timeout
     * @param onError         onError handler
     * @param <T>             T
     * @return callback
     */
    public <T> AsyncFutureCallback<T> callbackWithTimeoutAndErrorHandler(Class<T> cls,
                                                                         final Callback<T> callback,
                                                                         final long timeoutDuration,
                                                                         final TimeUnit timeUnit,
                                                                         final Consumer<Throwable> onError) {


        return callbackWithTimeoutAndErrorHandler(callback, timeoutDuration, timeUnit, onError);
    }

    /**
     * Add the coordinator to our list of coordinators.
     *
     * @param coordinator coordinator
     * @return coordinator
     */
    public CallbackCoordinator coordinate(final CallbackCoordinator coordinator) {

        this.coordinatorQueue.add(coordinator);
        return coordinator;
    }

    /**
     * Remove a coordinator from the list of coordinators that we are managing.
     *
     * @param coordinator coordinator
     * @return coordinator
     */
    public CallbackCoordinator removeCoordinator(final CallbackCoordinator coordinator) {

        this.removeCoordinatorQueue.add(coordinator);
        return coordinator;
    }

    /**
     * Remove a callback from the list of callbacks that we are managing.
     *
     * @param asyncFutureCallback asyncFutureCallback
     * @param <T>                 T
     * @return the callback that we removed.
     */
    public <T> AsyncFutureCallback<T> removeFuture(AsyncFutureCallback<T> asyncFutureCallback) {

        this.removeFutureQueue.offer(asyncFutureCallback);
        return asyncFutureCallback;
    }

    /**
     * Utility method to create a coordinator.
     *
     * @param coordinator     coordinator
     * @param startTime       startTime
     * @param timeoutDuration timeoutDuration
     * @param timeUnit        timeUnit
     * @param timeOutHandler  timeOutHandler
     * @param finishedHandler finishedHandler
     * @return callback coordinator
     */
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
                if (finishedHandler != null) {
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

    /**
     * Monitors timeouts. If the callback timed-out trigger it, and then remove callback from the list.
     */
    private void monitorCallBacks() {

        if (futureList.size() > 0) {

            final List<AsyncFutureCallback<?>> removeList = new ArrayList<>(futureList.size());
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
    }

    /**
     * Monitors Callback Coordinators.
     * Trigger timeouts if needed and remove coordinators from list that timed out.
     */
    private void monitorCallbackCoordinators() {


        if (coordinatorList.size() > 0) {
            final List<CallbackCoordinator> removeList = new ArrayList<>(coordinatorList.size());

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

    /**
     * Used for quickly delegating one callback to another.
     * <p>
     * This allows one liners so you don't have to create a builder for this common case.
     *
     * @param operationDescription Describe the operation for logging
     * @param callback             callback to delegate error and timeouts too.
     * @param logger               logger to log errors and timeouts.
     * @param <T>                  Generic type
     * @return wrapped callback that is tied to this reactor.
     */
    public <T> Callback<T> wrapCallback(final String operationDescription,
                                        final Callback<T> callback,
                                        final Logger logger) {

        /* Set the callback to delegate to this callback. */
        Callback<T> reactiveCallback = callbackBuilder().withCallback(new Callback<T>() {
            @Override
            public void accept(T t) {
                if (logger.isDebugEnabled()) {
                    logger.debug("{} returned {}", operationDescription, t);
                }
                callback.returnThis(t);
            }

        /* Provide some boiler plate error handling. */
        }).withErrorHandler(error -> {
            logger.error(String.format("ERROR calling %s", operationDescription), error);
            callback.onError(error);

        /* Provide some boiler timeout handling. */
        }).withTimeoutHandler(() -> {
            logger.error("TIMEOUT calling {}", operationDescription);
            callback.onTimeout();
        })
                .build();

        return reactiveCallback;
    }

    /**
     * Used for quickly delegating one callback to another when the return types are different.
     * This is usually the case if you want to do some transformation of the object and not just return it.
     * <p>
     * This allows one liners so you don't have to create a builder for this common case.
     *
     * @param operationDescription Describe the operation for logging
     * @param logger               logger to log errors and timeouts.
     * @param errorHandler         Callback that does the error handling if the delegated call fails.
     * @param callback             callback to delegate error and timeouts too.
     * @param <T>                  Generic type
     * @return wrapped callback that is tied to this reactor.
     */
    public <T> Callback<T> wrapCallbackErrors(final String operationDescription,
                                              final Callback<T> callback,
                                              final Callback<?> errorHandler,
                                              final Logger logger) {
        return callbackBuilder().setCallback(new Callback<T>() {
            @Override
            public void accept(T t) {
                if (logger.isDebugEnabled()) {
                    logger.debug("{} returned {}", operationDescription, t);
                }
                callback.returnThis(t);
            }

        /* Provide some boiler plate error handling. */
        }).setOnError(error -> {
            logger.error(String.format("ERROR calling %s", operationDescription), error);
            errorHandler.onError(error);
        }).setOnTimeout(() -> {
            logger.error("TIMEOUT calling {}", operationDescription);
            errorHandler.onTimeout();
        })
                .build();
    }

    /**
     * Used for quickly delegating one callback to another.
     * This allows one liners so you don't have to create a builder for this common case.
     *
     * @param operationDescription Describe the operation for logging
     * @param callback             callback to delegate error and timeouts too.
     * @param logger               logger to log errors and timeouts.
     * @param <T>                  Generic type
     * @param timeoutDuration      time out duration
     * @param timeUnit             Time Unit
     * @return wrapped callback that is tied to this reactor.
     */
    public <T> Callback<T> wrapCallbackWithTimeout(
            final String operationDescription,
            final Callback<T> callback,
            final Logger logger,
            final TimeUnit timeUnit,
            final long timeoutDuration) {
        return callbackBuilder().setCallback(new Callback<T>() {
            @Override
            public void accept(T t) {
                if (logger.isDebugEnabled()) {
                    logger.debug("{} returned {}", operationDescription, t);
                }
                callback.returnThis(t);
            }
        }).setOnError(error -> {
            logger.error(String.format("ERROR calling %s", operationDescription), error);
            callback.onError(error);

        }).setOnTimeout(() -> {
            logger.error("TIMEOUT calling {}", operationDescription);
            callback.onTimeout();
        }).setTimeoutTimeUnit(timeUnit).setTimeoutDuration(timeoutDuration)
                .build();
    }

    /**
     * Used for quickly delegating one callback to another when the return types are different.
     * This is usually the case if you want to do some transformation of the object and not just return it.
     *
     * @param operationDescription Describe the operation for logging
     * @param callback             callback to delegate error and timeouts too.
     * @param logger               logger to log errors and timeouts.
     * @param <T>                  Generic type
     * @param timeoutDuration      time out duration
     * @param timeUnit             Time Unit
     * @return wrapped callback that is tied to this reactor.
     */
    public <T> Callback<T> wrapCallbackErrorWithTimeout(
            final String operationDescription,
            final Callback<T> callback,
            final Callback<?> errorHandler,
            final Logger logger,
            final TimeUnit timeUnit,
            final long timeoutDuration) {
        return callbackBuilder().setCallback(new Callback<T>() {
            @Override
            public void accept(T t) {
                if (logger.isDebugEnabled()) {
                    logger.debug("{} returned {}", operationDescription, t);
                }
                callback.returnThis(t);
            }
        }).setOnError(error -> {
            logger.error(String.format("ERROR calling %s", operationDescription), error);
            errorHandler.onError(error);

        }).setOnTimeout(() -> {
            logger.error("TIMEOUT calling {}", operationDescription);
            errorHandler.onTimeout();
        }).withTimeoutTimeUnit(timeUnit).setTimeoutDuration(timeoutDuration)
                .build();
    }

    /**
     * A repeating task.
     */
    class RepeatingTask {
        private final Runnable task;
        private final long repeatEveryMS;
        private long lastTimeInvoked;


        public RepeatingTask(Runnable task, TimeUnit timeUnit, long repeatEvery) {
            this.task = task;
            this.repeatEveryMS = timeUnit.toMillis(repeatEvery);
        }
    }


    /**
     * Fire once task.
     */
    class FireOnceTask {
        private final Runnable task;
        private final long fireAfterMS;
        private final long created;

        public FireOnceTask(Runnable task, TimeUnit timeUnit, long fireAfter) {
            this.task = task;
            this.created = currentTime;
            this.fireAfterMS = timeUnit.toMillis(fireAfter);
        }
    }
}
