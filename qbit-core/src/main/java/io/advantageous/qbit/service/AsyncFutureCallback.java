package io.advantageous.qbit.service;


import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This allows for a callback to be called in the context of a service.
 * @author rhightower
 *
 */
public class AsyncFutureCallback<T> implements Runnable, Callback<T>, Future<T> {


    public static <T> AsyncFutureCallback<T> callback(final Callback<T> callback, long startTime, long maxExecutionTime) {
        return new AsyncFutureCallback<>(callback, startTime, maxExecutionTime);
    }

    private final Callback<T> callback;
    private final long startTime;
    private final long maxExecutionTime;
    private AtomicReference<T> value = new AtomicReference<>();
    private AtomicReference<Throwable> error = new AtomicReference<>();
    private AtomicBoolean cancelled = new AtomicBoolean();
    private AtomicBoolean done = new AtomicBoolean();
    public static final Exception CANCEL = new Exception("Cancelled RunnableCallback");

    public AsyncFutureCallback(final Callback<T> callback, long startTime, long maxExecutionDuration) {
        this.callback = callback;
        this.startTime = startTime;
        this.maxExecutionTime = maxExecutionDuration;

    }

    public boolean checkTimeOut(final long now) {
        if (now - startTime > maxExecutionTime) {
            callback.timedOut(startTime, now);
            return true;
        } else {
            return false;
        }
    }

    public void accept(final T t) {
        value.set(t);
        done.set(true);
    }

    public void onError(final Throwable error) {

        this.error.set(error);
        done.set(true);

    }

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
