package io.advantageous.qbit.reactive.async;

import io.advantageous.qbit.reactive.AsyncFutureCallback;
import io.advantageous.qbit.reactive.Callback;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Used to make async calls into sync calls if needed using a latch.
 */
public class AsyncFutureBlockOnGetCallbackImpl<T> implements AsyncFutureCallback<T> {


    private final Runnable onTimeout;
    private final Callback<T> callback;
    private final long startTime;
    private final long maxExecutionTime;
    private final Runnable onFinished;
    private final Consumer<Throwable> onError;
    private final AtomicReference<T> value = new AtomicReference<>();
    private final AtomicReference<Throwable> error = new AtomicReference<>();
    private final AtomicBoolean cancelled = new AtomicBoolean();
    private final AtomicBoolean done = new AtomicBoolean();
    private final AtomicBoolean timedOut = new AtomicBoolean();
    private final CountDownLatch latch = new CountDownLatch(1);

    public AsyncFutureBlockOnGetCallbackImpl(final Callback<T> callback,
                                             final long startTime,
                                             final long maxExecutionDuration,
                                             final Runnable onFinished,
                                             final Runnable onTimeout,
                                             final Consumer<Throwable> onError) {
        this.callback = callback;
        this.startTime = startTime;
        this.maxExecutionTime = maxExecutionDuration;
        this.onError = onError == null ? callback::onError : onError;
        this.onFinished = onFinished == null ? () -> {
        } : onFinished;

        this.onTimeout = onTimeout;

    }

    public static <T> AsyncFutureBlockOnGetCallbackImpl<T> callback(final Callback<T> callback,
                                                                    final long startTime,
                                                                    final long maxExecutionTime,
                                                                    final Runnable onFinished,
                                                                    final Runnable onTimeout,
                                                                    final Consumer<Throwable> onError) {
        return new AsyncFutureBlockOnGetCallbackImpl<>(callback, startTime, maxExecutionTime, onFinished, onTimeout, onError);
    }

    @Override
    public void finished() {
        onFinished.run();
    }

    public long timeOutDuration() {

        return maxExecutionTime;
    }


    public long startTime() {
        return startTime;
    }


    @Override
    public boolean checkTimeOut(final long now) {
        if (now - startTime > maxExecutionTime) {
            this.onTimeout();
            timedOut.set(true);
            return true;
        } else {
            return false;
        }
    }


    @Override
    public boolean isTimedOut() {
        return timedOut.get();
    }

    @Override
    public void accept(final T t) {
        value.set(t);
        done.set(true);
        latch.countDown();
    }

    @Override
    public void onError(final Throwable error) {

        this.error.set(error);
        done.set(true);
        latch.countDown();

    }

    @Override
    public void onTimeout() {
        if (done.get()) {
            return;
        }
        if (onTimeout == null) {
            callback.onTimeout();
        } else {
            onTimeout.run();
        }
        latch.countDown();
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    @Override
    public void run() {
        if (value.get() != null) {
            callback.accept(value.get());
        } else {
            if (error.get() != null) {
                onError.accept(error.get());
            } else {
                if (cancelled.get()) {
                    callback.onError(CANCEL);
                } else {
                    callback.accept(null);
                }
            }
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        cancelled.set(true);
        done.set(true);
        latch.countDown();
        return true;
    }

    @Override
    public boolean isCancelled() {
        return cancelled.get();
    }

    @Override
    public boolean isDone() {
        return done.get();
    }

    @Override
    public T get() {
        try {
            latch.await((long) (this.timeOutDuration() * 1.5), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.interrupted();
        }


        if (isTimedOut()) {
            throw new IllegalStateException("Timed out");
        }


        if (isCancelled()) {
            throw new IllegalStateException("Cancelled");
        }

        //noinspection ThrowableResultOfMethodCallIgnored
        if (error.get() != null) {
            throw new IllegalStateException("Operation failed", error.get());
        }

        return value.get();
    }


    @Override
    public T get(long timeout, TimeUnit unit) {

        try {
            latch.await(timeout, unit);
        } catch (InterruptedException e) {
            Thread.interrupted();
        }


        if (isTimedOut()) {
            throw new IllegalStateException("Timed out");
        }


        if (isCancelled()) {
            throw new IllegalStateException("Cancelled");
        }

        //noinspection ThrowableResultOfMethodCallIgnored
        if (error.get() != null) {
            throw new IllegalStateException("Operation failed", error.get());
        }

        return value.get();
    }

}
