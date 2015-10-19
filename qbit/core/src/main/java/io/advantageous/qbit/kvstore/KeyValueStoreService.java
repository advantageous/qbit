package io.advantageous.qbit.kvstore;

import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.time.Duration;

import java.util.Optional;

/** Low level key value store with expiration timeout. */
public interface KeyValueStoreService {





    void putString(final String key,
                        final String value);

    void putStringWithConfirmation(
                                   final Callback<Boolean> confirmation,
                                   final String key,
                                   final String value);

    void putStringWithConfirmationAndTimeout(
            final Callback<Boolean> confirmation,
            final String key,
            final String value,
            final Duration expiry);

    void putStringWithTimeout(
            final String key,
            final String value,
            final Duration expiry);

    void getString(
            final Callback<Optional<String>> confirmation,
            final String key);



    void putBytes(final String key,
                   final byte[] value);

    void putBytesWithConfirmation(
            final Callback<Boolean> confirmation,
            final String key,
            final byte[] value);

    void putBytesWithConfirmationAndTimeout(
            final Callback<Boolean> confirmation,
            final String key,
            final byte[] value,
            final Duration expiry);

    void putBytesWithTimeout(
            final String key,
            final byte[] value,
            final Duration expiry);

    void getBytes(
            final Callback<Optional<byte[]>> confirmation,
            final String key);

    void hasKey(
            final Callback<Boolean> hasKeyCallback,
            final String key);


    void delete(final String key);


    void deleteWithConfirmation(final Callback<Boolean> confirmation,
                                      final String key);



}
