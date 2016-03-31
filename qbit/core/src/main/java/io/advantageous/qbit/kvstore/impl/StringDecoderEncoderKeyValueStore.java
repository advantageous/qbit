package io.advantageous.qbit.kvstore.impl;

import io.advantageous.qbit.annotation.QueueCallback;
import io.advantageous.qbit.annotation.QueueCallbackType;
import io.advantageous.qbit.kvstore.KeyValueStoreService;
import io.advantageous.qbit.kvstore.lowlevel.LowLevelKeyValueStoreService;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.reactive.CallbackBuilder;
import io.advantageous.qbit.reactive.Reactor;
import io.advantageous.qbit.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.Function;

/**
 * allows you to specify an encoder and decoder to convert objects to/fro Strings
 *
 * @param <T>
 */
public class StringDecoderEncoderKeyValueStore<T> implements KeyValueStoreService<T> {


    /**
     * Logger.
     */
    private final Logger logger = LoggerFactory.getLogger(StringDecoderEncoderKeyValueStore.class);


    /**
     * Decoder.
     */
    private final Function<String, T> fromStringFunction;

    /**
     * Encoder.
     */
    private final Function<T, String> toStringFunction;

    /**
     * Holds the kv store.
     */
    private final LowLevelKeyValueStoreService kvStore;

    /**
     * Holds the reactor.
     */
    private final Reactor reactor;

    public StringDecoderEncoderKeyValueStore(Function<String, T> fromJsonFunction,
                                             Function<T, String> toJsonFunction,
                                             LowLevelKeyValueStoreService lowLevelKeyValueStoreService,
                                             Reactor reactor) {
        this.fromStringFunction = fromJsonFunction;
        this.toStringFunction = toJsonFunction;
        this.kvStore = lowLevelKeyValueStoreService;
        this.reactor = reactor;
    }

    @Override
    public void put(final String key, final T value) {

        kvStore.putString(key, toStringFunction.apply(value));
    }

    @Override
    public void putWithConfirmation(Callback<Boolean> confirmation, String key, T value) {
        kvStore.putStringWithConfirmation(
                reactor.wrapCallback(String.format("put key %s", key), confirmation, logger),
                key, toStringFunction.apply(value));

    }

    @Override
    public void putWithConfirmationAndTimeout(Callback<Boolean> confirmation, String key, T value, Duration expiry) {
        kvStore.putStringWithConfirmationAndTimeout(
                reactor.wrapCallback(String.format("put key %s %s", key, expiry), confirmation, logger),
                key, toStringFunction.apply(value), expiry);


    }

    @Override
    public void putWithTimeout(String key, T value, Duration expiry) {
        kvStore.putStringWithTimeout(key, toStringFunction.apply(value), expiry);
    }

    @Override
    public void get(final Callback<Optional<T>> callback, final String key) {


        final CallbackBuilder callbackBuilder = reactor.callbackBuilder().delegateWithLogging(callback,
                logger, String.format("Getting key %s", key));

        callbackBuilder.withCallback(Optional.class, value -> {
            if (value.isPresent()) {
                callback.accept(Optional.of(fromStringFunction.apply(value.get().toString())));
            } else {
                callback.accept(Optional.<T>empty());
            }
        });

        kvStore.getString(callbackBuilder.build(), key);
    }

    @Override
    public void hasKey(Callback<Boolean> hasKeyCallback, String key) {
        kvStore.hasKey(
                reactor.wrapCallback(String.format("has key key %s", key), hasKeyCallback, logger),
                key);
    }

    @Override
    public void delete(String key) {
        kvStore.delete(key);

    }

    @Override
    public void deleteWithConfirmation(final Callback<Boolean> confirmation, String key) {

        kvStore.deleteWithConfirmation(
                reactor.wrapCallback(String.format("delete key %s", key), confirmation, logger),
                key);
    }


    @QueueCallback({QueueCallbackType.EMPTY, QueueCallbackType.LIMIT, QueueCallbackType.IDLE})
    public void process() {
        kvStore.process();
        reactor.process();
    }
}
