package io.advantageous.qbit.kvstore;


import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.time.Duration;

import java.util.Optional;

/**
 * @param <T> T
 */
public interface KeyValueStoreService<T> {


    /**
     * Store a value in the kv store.
     *
     * @param key   key
     * @param value value
     */
    default void put(final String key, final T value) {
    }

    /**
     * Store a value and get a confirmation that it was stored.
     *
     * @param confirmation confirmation
     * @param key          key
     * @param value        value
     */
    default void putWithConfirmation(final Callback<Boolean> confirmation,
                                     final String key,
                                     final T value) {
        confirmation.accept(true);
    }

    /**
     * Store a key value with an expiry ad confirmation.
     *
     * @param confirmation confirmation
     * @param key          key
     * @param value        value
     * @param expiry       expiry
     */
    default void putWithConfirmationAndTimeout(
            final Callback<Boolean> confirmation,
            final String key,
            final T value,
            final Duration expiry) {
        confirmation.accept(true);
    }

    /**
     * Store a key value with a timeout expiry.
     *
     * @param key    key
     * @param value  value
     * @param expiry expiry
     */
    default void putWithTimeout(
            final String key,
            final T value,
            final Duration expiry) {

    }


    /**
     * Get a String key value.
     *
     * @param callback callback
     * @param key      key
     */
    default void get(final Callback<Optional<T>> callback,
                     final String key) {
        callback.returnThis(Optional.<T>empty());
    }


    /**
     * Check to see if the store has the key
     *
     * @param hasKeyCallback hasKeyCallback
     * @param key            key
     */
    default void hasKey(final Callback<Boolean> hasKeyCallback,
                        final java.lang.String key) {
        hasKeyCallback.returnThis(false);
    }


    /**
     * Delete the key.
     *
     * @param key key
     */
    default void delete(final String key) {
    }


    /**
     * Delete the key with confirmation.
     *
     * @param key key
     */
    default void deleteWithConfirmation(final Callback<Boolean> confirmation,
                                        final java.lang.String key) {
        confirmation.returnThis(true);
    }


    /**
     * Only used for local caches, not remote.
     */
    default void wipeCache() {

    }

    default void process() {

    }
}
