package io.advantageous.qbit.reactive;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Callback Builder
 * Created by rhightower on 3/23/15.
 */
public class CallbackBuilder  {

    private final Reactor reactor;
    private Callback callback;
    private Runnable onTimeout;
    private long timeoutDuration = -1;
    private TimeUnit timeoutTimeUnit = TimeUnit.SECONDS;
    private Consumer<Throwable> onError;

    public static CallbackBuilder callbackBuilder (final Reactor reactor) {
        return new CallbackBuilder(reactor);
    }

    public CallbackBuilder(final Reactor reactor) {
        this.reactor = reactor;
    }


    public Reactor getReactor() {
        return reactor;
    }

    public <T> Callback<T> getCallback() {
        return callback;
    }

    public CallbackBuilder setCallback(Callback callback) {
        this.callback = callback;
        return this;
    }

    public <T> CallbackBuilder setCallback(Class<T> returnType, Callback<T> callback) {
        this.callback = callback;
        return this;
    }


    public Runnable getOnTimeout() {
        return onTimeout;
    }

    public CallbackBuilder setOnTimeout(Runnable onTimeout) {
        this.onTimeout = onTimeout;
        return this;
    }

    public long getTimeoutDuration() {
        return timeoutDuration;
    }

    public CallbackBuilder setTimeoutDuration(long timeoutDuration) {
        this.timeoutDuration = timeoutDuration;
        return this;
    }

    public TimeUnit getTimeoutTimeUnit() {
        return timeoutTimeUnit;
    }

    public CallbackBuilder setTimeoutTimeUnit(TimeUnit timeoutTimeUnit) {
        this.timeoutTimeUnit = timeoutTimeUnit;
        return this;
    }

    public Consumer<Throwable> getOnError() {
        return onError;
    }

    public CallbackBuilder setOnError(Consumer<Throwable> onError) {
        this.onError = onError;
        return this;
    }


    public <T> AsyncFutureCallback<T> build() {


        if (getOnError()!=null || getOnTimeout() !=null || timeoutDuration != -1) {

            if (timeoutDuration == -1) {
                timeoutDuration = 30;
            }


            return reactor.callbackWithTimeoutAndErrorHandlerAndOnTimeout(
                    (Callback<T>) ((Object) getCallback()),
                    getTimeoutDuration(),
                    getTimeoutTimeUnit(),
                    getOnTimeout(),
                    getOnError());

        }

        return reactor.callback(this.callback);
    }

    public <T> AsyncFutureCallback<T> build(Class<T> returnType) {

        return build();
    }



    public <T> AsyncFutureCallback<T> build(Callback<T> callback) {

        this.setCallback(callback);

        return build();
    }




}
