package io.advantageous.qbit.reactive.async;


import io.advantageous.qbit.reactive.AsyncFutureCallback;
import io.advantageous.qbit.reactive.Callback;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * This allows for a callback to be called in the context of a service.
 * This is the original AsyncFutureCallbackImpl it does not support a synchronous get.
 *
 * @author rhightower
 */
public class AsyncFutureCallbackImpl<T> implements AsyncFutureCallback<T> {


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

    public AsyncFutureCallbackImpl(final Callback<T> callback,
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

    public static <T> AsyncFutureCallbackImpl<T> callback(final Callback<T> callback,
                                                          final long startTime,
                                                          final long maxExecutionTime,
                                                          final Runnable onFinished,
                                                          final Runnable onTimeout,
                                                          final Consumer<Throwable> onError) {
        return new AsyncFutureCallbackImpl<>(callback, startTime, maxExecutionTime, onFinished, onTimeout, onError);
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
    }

    @Override
    public void onError(final Throwable error) {

        this.error.set(error);
        done.set(true);

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
        //noinspection ThrowableResultOfMethodCallIgnored
        if (error.get() != null) {
            throw new IllegalStateException(error.get());
        }
        return value.get();
    }


    @Override
    public T get(long timeout, TimeUnit unit) {
        throw new UnsupportedOperationException("Not supported");
    }
}
