package io.advantageous.qbit.kvstore;

import io.advantageous.qbit.reactive.Callback;

import java.util.Optional;

public interface FallbackReader<T> {

    default void get(Callback<Optional<T>> callback, String key) {
        callback.returnThis(Optional.<T>empty());
    }

    default void hasKey(Callback<Boolean> callback, String key) {
        callback.returnThis(false);
    }

    default void flushRequests() {

    }
}
