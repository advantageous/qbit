package io.advantageous.qbit.reactive;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * You need this is you want to do error handling (Exception) from a callback.
 * Callback Builder
 * created by rhightower on 3/23/15.
 */
@SuppressWarnings("UnusedReturnValue")
public class CallbackBuilder {

    private Reactor reactor;
    private Callback callback;
    private Runnable onTimeout;
    private long timeoutDuration = -1;
    private TimeUnit timeoutTimeUnit = TimeUnit.SECONDS;
    private Consumer<Throwable> onError;

    /**
     * Deprecated.  this will become private.  Use the static method to get one.
     * @param reactor reactor
     */
    @Deprecated
    public CallbackBuilder(final Reactor reactor) {
        this.reactor = reactor;
    }

    /**
     * Deprecated.  this will become private.  Use the static method to get one.
     */
    @Deprecated
    public CallbackBuilder() {
    }

    /**
     * Deprecated.  use newBuilderWithReactor(Reactor r) instead
     *
     * @param reactor reactor
     * @return
     */
    @Deprecated
    public static CallbackBuilder callbackBuilder(final Reactor reactor) {
        return new CallbackBuilder(reactor);
    }

    public static CallbackBuilder newCallbackBuilderWithReactor(final Reactor reactor) {
        return new CallbackBuilder(reactor);
    }

    /**
     * Deprecated.  use newBuilder() instead
     *
     * @return
     */
    @Deprecated
    public static CallbackBuilder callbackBuilder() {
        return new CallbackBuilder();
    }

    public static CallbackBuilder newCallbackBuilder() {
        return new CallbackBuilder();
    }

    /**
     * This is Deprecated. this will become private. Builders should be only used to build in a local scope so this is
     * something that you should have just set.
     *
     * @return
     */
    @Deprecated
    public Reactor getReactor() {
        return reactor;
    }

    /**
     * This is Deprecated.  this will become private.  Builders should be only used to build in a local scope so this is
     * something that you should have just set.
     *
     * @return callback
     */
    @Deprecated
    public <T> Callback<T> getCallback() {
        //noinspection unchecked
        return callback;
    }

    /**
     * Builder method to add a callback handler.  This is depricated.  Use withCallback instead.
     *
     * @param callback callback
     * @return this
     */
    @Deprecated
    public CallbackBuilder setCallback(Callback callback) {
        this.callback = callback;
        return this;
    }

    /**
     * Builder method to add a callback handler.  This is depricated.  Use withCallback instead.
     *
     * @param returnType returnType
     * @param callback callback
     * @param <T> T
     * @return this
     */
    @Deprecated
    public <T> CallbackBuilder setCallback(Class<T> returnType, Callback<T> callback) {
        return withCallback(returnType, callback);
    }

    /**
     * Builder method to set the callback handler.
     *
     * @param returnType
     * @param callback
     * @param <T>
     * @return
     */
    public <T> CallbackBuilder withCallback(Class<T> returnType, Callback<T> callback) {
        this.callback = callback;
        return this;
    }

    /**
     * Builder method to set the callback handler.
     *
     * @param callback callback
     * @param <T> T
     * @return callback
     */
    public <T> CallbackBuilder withCallback(Callback<T> callback) {
        this.callback = callback;
        return this;
    }

    public <T> CallbackBuilder setCallbackReturnsList(Class<T> componentClass, Callback<List<T>> callback) {
        this.callback = callback;
        return this;
    }

    public <T> CallbackBuilder setCallbackReturnsSet(Class<T> componentClass, Callback<Set<T>> callback) {
        this.callback = callback;
        return this;
    }

    public <T> CallbackBuilder setCallbackReturnsCollection(Class<T> componentClass, Callback<Collection<T>> callback) {
        this.callback = callback;
        return this;
    }

    public <K, V> CallbackBuilder setCallbackReturnsMap(Class<K> keyClass, Class<V> valueClass,
                                                        Callback<Map<K, V>> callback) {
        this.callback = callback;
        return this;
    }





    /**
     * This is Deprecated.  this will become private.  Builders should be only used to build in a local scope so this is
     * something that you should have just set.
     *
     * @return
     */
    @Deprecated
    public Runnable getOnTimeout() {
        return onTimeout;
    }

    /**
     * Deprecated.  use withTimeoutHandler instead.
     *
     * @param onTimeout onTimeout
     * @return
     */
    @Deprecated
    public CallbackBuilder setOnTimeout(final Runnable onTimeout) {
        this.onTimeout = onTimeout;
        return this;
    }

    /**
     * Add a timeout handler to the callback.
     *
     * @param timeoutHandler timeoutHandler
     * @return
     */
    public CallbackBuilder withTimeoutHandler(final Runnable timeoutHandler) {
        this.onTimeout = timeoutHandler;
        return this;
    }

    /**
     * This is Deprecated.  this will become private.  Builders should be only used to build in a local scope so this is
     * something that you should have just set.
     *
     * @return
     */
    @Deprecated
    public long getTimeoutDuration() {
        return timeoutDuration;
    }

    /**
     * Deprecated. use withTimeoutInstead
     * @param timeoutDuration
     * @return
     */
    @Deprecated
    public CallbackBuilder setTimeoutDuration(@SuppressWarnings("SameParameterValue") long timeoutDuration) {
        this.timeoutDuration = timeoutDuration;
        return this;
    }

    public CallbackBuilder withTimeout(@SuppressWarnings("SameParameterValue") long timeoutDuration) {
        this.timeoutDuration = timeoutDuration;
        return this;
    }

    /**
     * This is Deprecated.  this will become private.  Builders should be only used to build in a local scope so this is
     * something that you should have just set.
     *
     * @return
     */
    @Deprecated
    public TimeUnit getTimeoutTimeUnit() {
        return timeoutTimeUnit;
    }

    /**
     * Deprecated.  use withTimeoutTimeUnit instead.
     * @param timeoutTimeUnit
     * @return
     */
    @Deprecated
    public CallbackBuilder setTimeoutTimeUnit(TimeUnit timeoutTimeUnit) {
        this.timeoutTimeUnit = timeoutTimeUnit;
        return this;
    }

    public CallbackBuilder withTimoutTimeUnit(TimeUnit timeoutTimeUnit) {
        this.timeoutTimeUnit = timeoutTimeUnit;
        return this;
    }

    /**
     * This is Deprecated.  this will become private.  Builders should be only used to build in a local scope so this is
     * something that you should have just set.
     *
     * @return
     */
    @Deprecated
    public Consumer<Throwable> getOnError() {
        return onError;
    }

    /**
     * Deprecated. use withErrorHandler instead.
     *
     * @return
     */
    @Deprecated
    public CallbackBuilder setOnError(Consumer<Throwable> onError) {
        this.onError = onError;
        return this;
    }

    /**
     * Add an error handler to the callback.
     *
     * @param onError
     * @return
     */
    public CallbackBuilder withErrorHandler(Consumer<Throwable> onError) {
        this.onError = onError;
        return this;
    }

    public <T> AsyncFutureCallback<T> build() {

        if (getOnError() != null || getOnTimeout() != null || timeoutDuration != -1) {

            if (timeoutDuration == -1) {
                timeoutDuration = 30;
            }

            if (reactor != null) {
                //noinspection unchecked
                return reactor.callbackWithTimeoutAndErrorHandlerAndOnTimeout(
                        (Callback<T>) getCallback(),
                        getTimeoutDuration(),
                        getTimeoutTimeUnit(),
                        getOnTimeout(),
                        getOnError());
            } else {
                return new AsyncFutureCallback<T>() {

                    @Override
                    public boolean checkTimeOut(long now) {

                        throw new IllegalStateException("You need to register a reactor to use this feature");
                    }

                    @Override
                    public void run() {
                        throw new IllegalStateException("You need to register a reactor to use this feature");

                    }

                    @Override
                    public boolean cancel(boolean mayInterruptIfRunning) {
                        throw new IllegalStateException("You need to register a reactor to use this feature");
                    }

                    @Override
                    public boolean isCancelled() {
                        throw new IllegalStateException("You need to register a reactor to use this feature");
                    }

                    @Override
                    public boolean isDone() {
                        throw new IllegalStateException("You need to register a reactor to use this feature");
                    }

                    @Override
                    public T get() {
                        throw new IllegalStateException("You need to register a reactor to use this feature");
                    }

                    @Override
                    public T get(long timeout, TimeUnit unit) {
                        throw new IllegalStateException("You need to register a reactor to use this feature");
                    }

                    @Override
                    public void accept(T t) {
                        getCallback().accept(t);
                    }

                    @Override
                    public void onError(final Throwable error) {

                        getOnError().accept(error);
                    }

                    @Override
                    public void onTimeout() {

                        getOnTimeout().run();
                    }
                };
            }

        }

        if (reactor != null) {
            return reactor.callback(this.getCallback());
        } else {

            final Callback callback = this.getCallback();
            return new AsyncFutureCallback<T>() {
                @Override
                public boolean checkTimeOut(long now) {
                    return false;
                }

                @Override
                public void accept(T t) {

                    callback.accept(t);
                }

                @Override
                public void onError(Throwable error) {

                    callback.onError(error);
                }

                @Override
                public void run() {

                }

                @Override
                public boolean cancel(boolean mayInterruptIfRunning) {
                    throw new IllegalStateException("You need to register a reactor to use this feature");

                }

                @Override
                public boolean isCancelled() {
                    throw new IllegalStateException("You need to register a reactor to use this feature");
                }

                @Override
                public boolean isDone() {
                    throw new IllegalStateException("You need to register a reactor to use this feature");
                }

                @Override
                public T get() {
                    throw new IllegalStateException("You need to register a reactor to use this feature");
                }

                @Override
                public T get(long timeout, TimeUnit unit) {
                    throw new IllegalStateException("You need to register a reactor to use this feature");
                }
            };
        }
    }

    public <T> AsyncFutureCallback<T> build(Class<T> returnType) {

        return build();
    }


    public <T> AsyncFutureCallback<T> build(Callback<T> callback) {

        this.setCallback(callback);

        return build();
    }


}
