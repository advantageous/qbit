package io.advantageous.qbit.reactive;

import org.slf4j.Logger;

import java.util.*;
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
    private boolean supportLatch;


    /**
     * @param reactor reactor
     */
    private CallbackBuilder(final Reactor reactor) {
        this.reactor = reactor;
    }

    /**
     *
     */
    private CallbackBuilder() {
    }

    /**
     * @param reactor reactor
     * @return CallbackBuilder
     */
    public static CallbackBuilder callbackBuilder(final Reactor reactor) {
        return new CallbackBuilder(reactor);
    }


    /**
     * Creating callback builder.
     *
     * @param reactor reactor
     * @return CallbackBuilder
     */
    public static CallbackBuilder newCallbackBuilderWithReactor(final Reactor reactor) {
        return new CallbackBuilder(reactor);
    }

    /**
     * @return CallbackBuilder
     */
    public static CallbackBuilder callbackBuilder() {
        return new CallbackBuilder();
    }


    /**
     * @return CallbackBuilder
     */
    public static CallbackBuilder newCallbackBuilder() {
        return new CallbackBuilder();
    }

    /**
     * @return Reactor
     */
    public Reactor getReactor() {
        return reactor;
    }

    /**
     * @return callback
     */
    public <T> Callback<T> getCallback() {
        //noinspection unchecked
        return callback;
    }

    /**
     * Builder method to add a callback handler.
     *
     * @param callback callback
     * @return this
     */
    public CallbackBuilder setCallback(final Callback callback) {
        this.callback = callback;
        return this;
    }

    /**
     * Builder method to add a callback handler.
     *
     * @param returnType returnType
     * @param callback   callback
     * @param <T>        T
     * @return this
     */
    public <T> CallbackBuilder setCallback(final Class<T> returnType, final Callback<T> callback) {
        return withCallback(returnType, callback);
    }

    /**
     * Builder method to set the callback handler.
     *
     * @param returnType returnType
     * @param callback   callback
     * @param <T>        T
     * @return this
     */
    public <T> CallbackBuilder withCallback(final Class<T> returnType,
                                            final Callback<T> callback) {
        this.callback = callback;
        return this;
    }


    /**
     * Builder method to delegate timeout and error handling to other callback.
     *
     * @param callback callback
     * @param <T>      T
     * @return this
     */
    public <T> CallbackBuilder delegate(final Callback<T> callback) {

        this.withErrorHandler(callback::onError);

        this.withTimeoutHandler(callback::onTimeout);

        return this;
    }

    /**
     * Builder method to wrap and delegate, timeout and error handling and callback itself.
     *
     * @param callback callback
     * @param <T>      T
     * @return this
     */
    public <T> CallbackBuilder wrap(final Callback<T> callback) {

        this.withErrorHandler(callback::onError);

        this.withTimeoutHandler(callback::onTimeout);

        this.withCallback(callback);

        return this;
    }


    /**
     * Builder method to delegate timeout and error handling to other callback.
     *
     * @param callback callback
     * @param <T>      T
     * @return this
     */
    public <T> CallbackBuilder delegateWithLogging(final Callback<T> callback, final Logger logger,
                                                   final String operationName) {

        this.withErrorHandler(throwable -> {
            logger.error(operationName + " ERROR ", throwable);
            callback.onError(throwable);
        });

        this.withTimeoutHandler(() -> {
            logger.error(operationName + " TIMED OUT ");
            callback.onTimeout();
        });

        return this;
    }

    /**
     * Builder method to wrap / delegate timeout and error handling as well as callback itself.
     *
     * @param callback callback
     * @param <T>      T
     * @return this
     */
    public <T> CallbackBuilder wrapWithLogging(final Callback<T> callback, final Logger logger,
                                               final String operationName) {

        this.withErrorHandler(throwable -> {
            logger.error(operationName + " error ", throwable);
            callback.onError(throwable);
        });

        this.withTimeoutHandler(() -> {
            logger.error(operationName + " TIMED OUT ");
            callback.onTimeout();
        });


        this.withCallback(callback);
        return this;
    }


    /**
     * Builder method to set the callback handler.
     *
     * @param callback callback
     * @param <T>      T
     * @return callback
     */
    public <T> CallbackBuilder withCallback(final Callback<T> callback) {
        this.callback = callback;
        return this;
    }

    /**
     * Builder method to set callback handler that takes a list
     *
     * @param componentClass componentClass
     * @param callback       callback
     * @param <T>            T
     * @return this
     */
    public <T> CallbackBuilder withListCallback(final Class<T> componentClass,
                                                final Callback<List<T>> callback) {
        this.callback = callback;
        return this;
    }


    /**
     * Builder method to set callback handler that takes a set
     *
     * @param componentClass componentClass
     * @param callback       callback
     * @param <T>            T
     * @return this
     */
    public <T> CallbackBuilder withSetCallback(final Class<T> componentClass,
                                               final Callback<Set<T>> callback) {
        this.callback = callback;
        return this;
    }


    /**
     * Builder method to set callback handler that takes a collection
     *
     * @param componentClass componentClass
     * @param callback       callback
     * @param <T>            T
     * @return this
     */
    public <T> CallbackBuilder withCollectionCallback(final Class<T> componentClass,
                                                      final Callback<Collection<T>> callback) {
        this.callback = callback;
        return this;
    }


    /**
     * Builder method to set callback handler that takes a map
     *
     * @param keyClass   keyClass
     * @param valueClass valueClass
     * @param callback   callback
     * @param <K>        key type
     * @param <V>        value type
     * @return this
     */
    public <K, V> CallbackBuilder withMapCallback(final Class<K> keyClass,
                                                  final Class<V> valueClass,
                                                  final Callback<Map<K, V>> callback) {
        this.callback = callback;
        return this;
    }


    /**
     * Builder method to set callback handler that takes a boolean
     *
     * @param callback callback
     * @return this
     */
    public CallbackBuilder withBooleanCallback(final Callback<Boolean> callback) {
        this.callback = callback;
        return this;
    }

    /**
     * Builder method to set callback handler that takes a integer
     *
     * @param callback callback
     * @return this
     */
    public CallbackBuilder withIntCallback(final Callback<Integer> callback) {
        this.callback = callback;
        return this;
    }


    /**
     * Builder method to set callback handler that takes a long
     *
     * @param callback callback
     * @return this
     */
    public CallbackBuilder withLongCallback(final Callback<Long> callback) {
        this.callback = callback;
        return this;
    }


    /**
     * Builder method to set callback handler that takes a string
     *
     * @param callback callback
     * @return this
     */
    public CallbackBuilder withStringCallback(final Callback<String> callback) {
        this.callback = callback;
        return this;
    }


    /**
     * Builder method to set callback handler that takes an optional string
     *
     * @param callback callback
     * @return this
     */
    public CallbackBuilder withOptionalStringCallback(final Callback<Optional<String>> callback) {
        this.callback = callback;
        return this;
    }


    /**
     * Builder method to set callback handler that takes an optional string
     *
     * @param callback callback
     * @return this
     */
    public <T> CallbackBuilder withOptionalCallback(final Class<T> cls, final Callback<Optional<T>> callback) {
        this.callback = callback;
        return this;
    }


    /**
     * @return runnable
     */
    public Runnable getOnTimeout() {
        return onTimeout;
    }

    /**
     * @param onTimeout onTimeout
     * @return this
     */
    public CallbackBuilder setOnTimeout(final Runnable onTimeout) {
        this.onTimeout = onTimeout;
        return this;
    }

    /**
     * Add a timeout handler to the callback.
     *
     * @param timeoutHandler timeoutHandler
     * @return this
     */
    public CallbackBuilder withTimeoutHandler(final Runnable timeoutHandler) {
        this.onTimeout = timeoutHandler;
        return this;
    }

    /**
     * @return timeout duration
     */
    public long getTimeoutDuration() {
        return timeoutDuration;
    }

    /**
     * @param timeoutDuration timeoutDuration
     * @return this
     */
    public CallbackBuilder setTimeoutDuration(@SuppressWarnings("SameParameterValue") long timeoutDuration) {
        this.timeoutDuration = timeoutDuration;
        return this;
    }


    /**
     * @param timeoutDuration timeoutDuration
     * @return this
     */
    public CallbackBuilder withTimeout(@SuppressWarnings("SameParameterValue") long timeoutDuration) {
        this.timeoutDuration = timeoutDuration;
        return this;
    }

    /**
     * @return time unit
     */
    public TimeUnit getTimeoutTimeUnit() {
        return timeoutTimeUnit;
    }

    /**
     * @param timeoutTimeUnit timeoutTimeUnit
     * @return this
     */
    public CallbackBuilder setTimeoutTimeUnit(final TimeUnit timeoutTimeUnit) {
        this.timeoutTimeUnit = timeoutTimeUnit;
        return this;
    }


    /**
     * @param timeoutTimeUnit timeoutTimeUnit
     * @return this
     */
    public CallbackBuilder withTimeoutTimeUnit(final TimeUnit timeoutTimeUnit) {
        this.timeoutTimeUnit = timeoutTimeUnit;
        return this;
    }

    /**
     * @return error handler
     */
    public Consumer<Throwable> getOnError() {
        return onError;
    }

    /**
     * @return this
     */
    public CallbackBuilder setOnError(Consumer<Throwable> onError) {
        this.onError = onError;
        return this;
    }

    /**
     * Add an error handler to the callback.
     *
     * @param onError onerror
     * @return this
     */
    public CallbackBuilder withErrorHandler(final Consumer<Throwable> onError) {
        this.onError = onError;
        return this;
    }

    public <T> AsyncFutureCallback<T> build() {

        if (getOnError() != null || getOnTimeout() != null) {

            if (timeoutDuration == -1) {
                timeoutDuration = 30;
            }

            if (reactor != null) {

                if (this.isSupportLatch()) {
                    return reactor.callbackWithTimeoutAndErrorHandlerAndOnTimeoutWithLatch(
                            (Callback<T>) getCallback(),
                            getTimeoutDuration(),
                            getTimeoutTimeUnit(),
                            getOnTimeout(),
                            getOnError());

                } else {
                    //noinspection unchecked
                    return reactor.callbackWithTimeoutAndErrorHandlerAndOnTimeout(
                            (Callback<T>) getCallback(),
                            getTimeoutDuration(),
                            getTimeoutTimeUnit(),
                            getOnTimeout(),
                            getOnError());
                }
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
            if (isSupportLatch()) {
                return reactor.callback(this.getCallback());
            } else {
                return reactor.callbackWithLatch(this.getCallback());
            }
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

        this.withCallback(callback);

        return build();
    }


    public boolean isSupportLatch() {
        return supportLatch;
    }

    public CallbackBuilder setSupportLatch(boolean supportLatch) {
        this.supportLatch = supportLatch;
        return this;
    }
}
