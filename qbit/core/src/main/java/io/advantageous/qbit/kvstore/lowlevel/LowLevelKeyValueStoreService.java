package io.advantageous.qbit.kvstore.lowlevel;

import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.time.Duration;

import java.util.Optional;

/**
 * Low level key value store with expiration timeout.
 */
public interface LowLevelKeyValueStoreService {


    /**
     * Store a string value in the kv store.
     *
     * @param key   key
     * @param value value
     */
    void putString(final String key, final String value);

    /**
     * Store a string value and get a confirmation that it was stored.
     *
     * @param confirmation confirmation
     * @param key          key
     * @param value        value
     */
    void putStringWithConfirmation(final Callback<Boolean> confirmation,
                                   final String key,
                                   final String value);

    /**
     * Store a key value with an expiry.
     *
     * @param confirmation confirmation
     * @param key          key
     * @param value        value
     * @param expiry       expiry
     */
    void putStringWithConfirmationAndTimeout(
            final Callback<Boolean> confirmation,
            final String key,
            final String value,
            final Duration expiry);

    /**
     * Store a key value with a timeout expiry.
     *
     * @param key    key
     * @param value  value
     * @param expiry expiry
     */
    void putStringWithTimeout(
            final String key,
            final String value,
            final Duration expiry);


    /**
     * Get a String key value.
     *
     * @param confirmation confirmation
     * @param key          key
     */
    void getString(final Callback<Optional<String>> confirmation,
                   final String key);


    /**
     * Store a byte array value in the kv store.
     *
     * @param key   key
     * @param value value
     */
    void putBytes(final String key,
                  final byte[] value);

    /**
     * Store a byte array value in the kv store with a confirmation callback.
     *
     * @param confirmation confirmation
     * @param key          key
     * @param value        value
     */
    void putBytesWithConfirmation(final Callback<Boolean> confirmation,
                                  final String key,
                                  final byte[] value);


    /**
     * Store a byte array value in the kv store with a
     * confirmation callback and expiry.
     *
     * @param confirmation confirmation
     * @param key          key
     * @param value        value
     * @param expiry       expiry
     */
    void putBytesWithConfirmationAndTimeout(final Callback<Boolean> confirmation,
                                            final String key,
                                            final byte[] value,
                                            final Duration expiry);

    /**
     * Store a byte array with an expiry.
     *
     * @param key    key
     * @param value  value
     * @param expiry expiry
     */
    void putBytesWithTimeout(final String key,
                             final byte[] value,
                             final Duration expiry);


    /**
     * Get a byte array value given the key.
     *
     * @param callback callback
     * @param key      key
     */
    void getBytes(final Callback<Optional<byte[]>> callback,
                  final String key);


    /**
     * Check to see if the store has the key
     *
     * @param hasKeyCallback hasKeyCallback
     * @param key            key
     */
    void hasKey(final Callback<Boolean> hasKeyCallback,
                final String key);


    /**
     * Delete the key.
     *
     * @param key key
     */
    void delete(final String key);


    /**
     * Delete the key with confirmation.
     *
     * @param key key
     */
    void deleteWithConfirmation(final Callback<Boolean> confirmation,
                                final String key);


    void process();

}
