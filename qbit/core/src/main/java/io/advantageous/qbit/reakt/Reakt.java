package io.advantageous.qbit.reakt;

import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.reactive.CallbackBuilder;
import io.advantageous.qbit.reactive.Reactor;
import io.advantageous.reakt.Result;
import io.advantageous.reakt.promise.Promise;

import java.util.concurrent.TimeoutException;

/**
 * Converts Reakt promises and callbacks into QBit callbacks.
 */
public class Reakt {

    /**
     * Converts a Reakt promise into a QBit callback.
     *
     * @param promise reakt promise
     * @param <T>     type of promise
     * @return new QBit callback that wraps promise.
     */
    public static <T> Callback<T> convertPromise(final Promise<T> promise) {
        return convertPromiseToCallback(promise, CallbackBuilder.callbackBuilder());
    }

    /**
     * Converts a Reakt promise into a QBit callback.
     * Reactor is used to manage timeouts and ensure callback happens on same thread as caller.
     *
     * @param reactor QBit reactor
     * @param promise reakt promise
     * @param <T>     type of promise
     * @return new QBit callback that wraps promise.
     */
    public static <T> Callback<T> convertPromise(final Reactor reactor, final Promise<T> promise) {
        return convertPromiseToCallback(promise, reactor.callbackBuilder());
    }

    /**
     * Converts a Reakt callback into a QBit callback.
     *
     * @param callback reakt callback
     * @param <T>      type of result
     * @return QBit callback
     */
    public static <T> Callback<T> convertCallback(final io.advantageous.reakt.Callback<T> callback) {
        return convertReaktCallbackToQBitCallback(callback, CallbackBuilder.callbackBuilder());
    }


    /**
     * Converts a Reakt callback into a QBit callback.
     *
     * @param callback reakt callback
     * @param <T>      type of result
     * @return QBit callback
     */
    public static <T> io.advantageous.reakt.Callback<T> convertQBitCallback(final Callback<T> callback) {
        return result -> {

            if (result.failure()) {
                callback.onError(result.cause());
            } else {
                callback.accept(result.get());
            }
        };
    }


    /**
     * Converts a Reakt callback into a QBit callback.
     * <p>
     * Reactor is used to manage timeouts and ensure callback happens on same thread as caller.
     *
     * @param reactor  QBit reactor
     * @param callback reakt callback
     * @param <T>      type of result
     * @return QBit callback
     */
    public static <T> Callback<T> convertCallback(final Reactor reactor,
                                                  final io.advantageous.reakt.Callback<T> callback) {
        return convertReaktCallbackToQBitCallback(callback, reactor.callbackBuilder());
    }

    private static <T> Callback<T> convertPromiseToCallback(final Promise<T> promise, CallbackBuilder callbackBuilder) {
        return callbackBuilder
                .withCallback(o -> promise.onResult(Result.result((T) o)))
                .withErrorHandler(throwable -> promise.onResult(Result.error(throwable)))
                .withTimeoutHandler(() -> promise.onResult(Result.error(new TimeoutException()))).build();
    }

    private static <T> Callback<T> convertReaktCallbackToQBitCallback(final io.advantageous.reakt.Callback<T> callback, CallbackBuilder callbackBuilder) {
        return callbackBuilder
                .withCallback(o -> callback.onResult(Result.result((T) o)))
                .withErrorHandler(throwable -> callback.onResult(Result.error(throwable)))
                .withTimeoutHandler(() -> callback.onResult(Result.error(new TimeoutException()))).build();

    }

}
