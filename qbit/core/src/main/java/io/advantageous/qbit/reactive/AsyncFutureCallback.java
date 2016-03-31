package io.advantageous.qbit.reactive;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * A callback that is also a future, but is an async future.
 * AsyncFutureCallback
 * created by rhightower on 3/22/15.
 */
public interface AsyncFutureCallback<T> extends Runnable, Callback<T>, Future<T> {
    Exception CANCEL = new Exception("Cancelled RunnableCallback");

    boolean checkTimeOut(long now);

    void accept(T t);

    void onError(Throwable error);

    void run();

    @Override
    boolean cancel(boolean mayInterruptIfRunning);

    @Override
    boolean isCancelled();

    @Override
    boolean isDone();

    @Override
    T get();

    @SuppressWarnings("NullableProblems")
    @Override
    T get(long timeout, TimeUnit unit);


    default boolean timedOut(long now) {

        return !(startTime() == -1 || timeOutDuration() == -1) && (now - startTime()) > timeOutDuration();
    }

    /**
     * Timeout duration in milliseconds.
     */
    default long timeOutDuration() {
        return -1;
    }


    default long startTime() {
        return -1;
    }

    default void finished() {

    }


    default boolean isTimedOut() {
        return false;
    }
}
