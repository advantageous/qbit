package io.advantageous.qbit.reactive.impl;


import io.advantageous.qbit.reactive.AsyncFutureCallback;
import io.advantageous.qbit.reactive.Callback;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This allows for a callbackWithTimeout to be called in the context of a service.
 * @author rhightower
 *
 */
public class AsyncFutureCallbackImpl<T> implements AsyncFutureCallback<T> {



    public static <T> AsyncFutureCallbackImpl<T> callback(final Callback<T> callback,
                                                          final long startTime,
                                                          final long maxExecutionTime,
                                                          final Runnable onFinished) {
        return new AsyncFutureCallbackImpl<>(callback, startTime, maxExecutionTime, onFinished);
    }

    private final Callback<T> callback;
    private final long startTime;
    private final long maxExecutionTime;
    private AtomicReference<T> value = new AtomicReference<>();
    private AtomicReference<Throwable> error = new AtomicReference<>();
    private AtomicBoolean cancelled = new AtomicBoolean();
    private AtomicBoolean done = new AtomicBoolean();
    private AtomicBoolean timedOut = new AtomicBoolean();
    private final Runnable onFinished;

    public AsyncFutureCallbackImpl(final Callback<T> callback,
                                   final long startTime,
                                   final long maxExecutionDuration,
                                   final Runnable onFinished) {
        this.callback = callback;
        this.startTime = startTime;
        this.maxExecutionTime = maxExecutionDuration;
        this.onFinished = onFinished == null ? () -> {
        } : onFinished;

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
            callback.timedOut(startTime, now);
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
    public void run() {
            if (value.get()!=null) {
                callback.accept(value.get());
            } else {
                if (error.get()!=null) {
                    callback.onError(error.get());
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
    public T get()  {
        if (error.get()!=null) {
            throw new IllegalStateException(error.get());
        }
        return value.get();
    }



    @Override
    public T get(long timeout, TimeUnit unit)  {
        throw new UnsupportedOperationException("Not supported");
    }
}
