package io.advantageous.qbit.kvstore;


import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.time.Duration;

import java.util.Optional;

/**
 *
 * @param <T> T
 */
public interface KeyValueStoreService<T> {


    /**
     * Store a value in the kv store.
     * @param key key
     * @param value value
     */
    void put(final String key, final T value);

    /**
     * Store a value and get a confirmation that it was stored.
     * @param confirmation confirmation
     * @param key key
     * @param value value
     */
    void putWithConfirmation(final Callback<Boolean> confirmation,
                                   final String key,
                                   final T value);

    /**
     * Store a key value with an expiry ad confirmation.
     * @param confirmation confirmation
     * @param key key
     * @param value value
     * @param expiry expiry
     */
    void putWithConfirmationAndTimeout(
            final Callback<Boolean> confirmation,
            final String key,
            final T value,
            final Duration expiry);

    /**
     * Store a key value with a timeout expiry.
     * @param key key
     * @param value value
     * @param expiry expiry
     */
    void putWithTimeout(
            final String key,
            final T value,
            final Duration expiry);


    /**
     * Get a String key value.
     * @param confirmation confirmation
     * @param key key
     */
    void get( final Callback<Optional<T>> confirmation,
                    final String key);


    /**
     * Check to see if the store has the key
     * @param hasKeyCallback hasKeyCallback
     * @param key key
     */
    void hasKey(    final Callback<Boolean> hasKeyCallback,
                    final java.lang.String key);


    /**
     * Delete the key.
     * @param key key
     */
    void delete(final String key);



    /**
     * Delete the key with confirmation.
     * @param key key
     */
    void deleteWithConfirmation(    final Callback<Boolean> confirmation,
                                    final java.lang.String key);


    void process();
}
