package io.advantageous.qbit.kvstore;


import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.time.Duration;

public interface WriteBehindWriter<T> {

    default void writeWithConfirmation(Callback<Boolean> confirmation, String key, T value) {
        confirmation.returnThis(true);
    }

    default void write(String key, T value) {

    }

    default void writeWithConfirmationAndTimeout(Callback<Boolean> confirmation, String key, T value, Duration expiry) {
        confirmation.returnThis(true);
    }

    default void writeWithTimeout(String key, T value, Duration expiry) {
    }

    default void delete(String key) {
    }


    default void deleteWithConfirmation(Callback<Boolean> confirmation, String key) {
        confirmation.returnThis(true);
    }


    default void flushRequests() {

    }
}
