package io.advantageous.qbit.kvstore.lowlevel;

import io.advantageous.qbit.annotation.QueueCallback;
import io.advantageous.qbit.annotation.QueueCallbackType;
import io.advantageous.qbit.kvstore.impl.StringDecoderEncoderKeyValueStore;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.reactive.CallbackBuilder;
import io.advantageous.qbit.reactive.CallbackCoordinator;
import io.advantageous.qbit.reactive.Reactor;
import io.advantageous.qbit.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Allows you to specify two kvstores that will be both written to and read from in order
 */
public class LowLevelWriteBehindReadFallbackKeyValueStore implements LowLevelKeyValueStoreService {


    /**
     * Logger.
     */
    private final Logger logger = LoggerFactory.getLogger(StringDecoderEncoderKeyValueStore.class);

    /**
     * Write to the local first, and read from it first.
     */
    private final LowLevelKeyValueStoreService localKeyValueStore;


    /**
     * Write to the remote last, and write to it last.
     */
    private final LowLevelKeyValueStoreService remoteKeyValueStore;


    /**
     * Reactor
     */
    private final Reactor reactor;

    public LowLevelWriteBehindReadFallbackKeyValueStore(LowLevelKeyValueStoreService localKeyValueStore,
                                                        LowLevelKeyValueStoreService remoteKeyValueStore,
                                                        Reactor reactor) {
        this.localKeyValueStore = localKeyValueStore;
        this.remoteKeyValueStore = remoteKeyValueStore;
        this.reactor = reactor;
        this.reactor.addServiceToFlush(this.localKeyValueStore);
        this.reactor.addServiceToFlush(this.remoteKeyValueStore);
    }


    private CallbackBuilder getCallbackBuilderForPut(Callback<Boolean> confirmation, String key) {
        final AtomicInteger count = new AtomicInteger();
        final AtomicBoolean failed = new AtomicBoolean();

        final CallbackBuilder callbackBuilder = reactor.callbackBuilder();

        callbackBuilder
                .withBooleanCallback(
                        success -> {
                            long currentCount = count.incrementAndGet();

                            logger.info("CURRENT COUNT {}", currentCount);
                        })
                .withErrorHandler(error -> {
                    logger.error(String.format("Failed to put key %s", key), error);
                    failed.set(true);
                    count.incrementAndGet();
                })
                .withTimeoutHandler(() -> {
                    logger.error(String.format("Timeout trying to put key %s", key));
                    failed.set(true);
                    confirmation.onTimeout();
                    count.incrementAndGet();
                });

        final CallbackCoordinator callbackCoordinator = () -> {

            if (count.get() >= 2) {

                logger.info("DONE PROCESSING");
                return true;
            }
            return false;

        };
        reactor.coordinatorBuilder()
                .setCoordinator(callbackCoordinator)
                .setFinishedHandler(() -> {

                    if (failed.get()) {
                        return;
                    }
                    confirmation.accept(true);
                }).setTimeOutHandler(() -> {

            if (failed.get()) {
                return;
            }
            failed.set(true);
            logger.error(String.format("Timeout trying to put key %s", key));
            confirmation.onTimeout();

        }).build();
        return callbackBuilder;
    }

    @Override
    public void putStringWithConfirmationAndTimeout(Callback<Boolean> confirmation, String key, String value, Duration expiry) {

        final CallbackBuilder callbackBuilder = getCallbackBuilderForPut(confirmation, key);
        localKeyValueStore.putStringWithConfirmationAndTimeout(callbackBuilder.build(), key, value, expiry);
        remoteKeyValueStore.putStringWithConfirmationAndTimeout(callbackBuilder.build(), key, value, expiry);
    }

    @Override
    public void putStringWithTimeout(String key, String value, Duration expiry) {
        localKeyValueStore.putStringWithTimeout(key, value, expiry);
        remoteKeyValueStore.putStringWithTimeout(key, value, expiry);
    }

    @Override
    public void getString(final Callback<Optional<String>> callback, final String key) {

        final CallbackBuilder callbackBuilderForLocal = reactor.callbackBuilder().delegateWithLogging(callback,
                logger, String.format("Get %s from local", key));

        callbackBuilderForLocal.withCallback(Optional.class, optional -> {
            if (optional.isPresent()) {
                callback.returnThis(optional);
            } else {

                final CallbackBuilder callbackBuilderForRemote = reactor.callbackBuilder().delegateWithLogging(callback,
                        logger, String.format("Get %s from remote", key));
                callbackBuilderForRemote.withCallback(Optional.class, callback::returnThis);
                remoteKeyValueStore.getString(callbackBuilderForRemote.build(), key);
            }
        });

        localKeyValueStore.getString(callbackBuilderForLocal.build(), key);

    }


    @Override
    public void putString(String key, String value) {

        localKeyValueStore.putString(key, value);
        remoteKeyValueStore.putString(key, value);
    }

    @Override
    public void putBytes(String key, byte[] value) {

        localKeyValueStore.putBytes(key, value);
        remoteKeyValueStore.putBytes(key, value);

    }


    @Override
    public void putStringWithConfirmation(final Callback<Boolean> confirmation,
                                          final String key,
                                          final String value) {

        final CallbackBuilder callbackBuilder = getCallbackBuilderForPut(confirmation, key);
        localKeyValueStore.putStringWithConfirmation(callbackBuilder.build(), key, value);
        remoteKeyValueStore.putStringWithConfirmation(callbackBuilder.build(), key, value);

    }

    @Override
    public void putBytesWithConfirmation(Callback<Boolean> confirmation, String key, byte[] value) {
        final CallbackBuilder callbackBuilder = getCallbackBuilderForPut(confirmation, key);
        localKeyValueStore.putBytesWithConfirmation(callbackBuilder.build(), key, value);
        remoteKeyValueStore.putBytesWithConfirmation(callbackBuilder.build(), key, value);

    }

    @Override
    public void putBytesWithConfirmationAndTimeout(Callback<Boolean> confirmation, String key, byte[] value, Duration expiry) {
        final CallbackBuilder callbackBuilder = getCallbackBuilderForPut(confirmation, key);
        localKeyValueStore.putBytesWithConfirmationAndTimeout(callbackBuilder.build(), key, value, expiry);
        remoteKeyValueStore.putBytesWithConfirmationAndTimeout(callbackBuilder.build(), key, value, expiry);

    }

    @Override
    public void putBytesWithTimeout(String key, byte[] value, Duration expiry) {
        localKeyValueStore.putBytesWithTimeout(key, value, expiry);
        remoteKeyValueStore.putBytesWithTimeout(key, value, expiry);

    }

    @Override
    public void getBytes(Callback<Optional<byte[]>> callback, String key) {

        final CallbackBuilder callbackBuilderForLocal = reactor.callbackBuilder().delegateWithLogging(callback,
                logger, String.format("Get %s from local", key));

        callbackBuilderForLocal.withCallback(Optional.class, optional -> {
            if (optional.isPresent()) {
                callback.returnThis(optional);
            } else {

                final CallbackBuilder callbackBuilderForRemote = reactor.callbackBuilder().delegateWithLogging(callback,
                        logger, String.format("Get %s from remote", key));
                callbackBuilderForRemote.withCallback(Optional.class, callback::returnThis);
                remoteKeyValueStore.getBytes(callbackBuilderForRemote.build(), key);
            }
        });

        localKeyValueStore.getBytes(callbackBuilderForLocal.build(), key);

    }

    @Override
    public void hasKey(final Callback<Boolean> hasKeyCallback, final String key) {

        final CallbackBuilder callbackBuilderForLocal = reactor.callbackBuilder().delegateWithLogging(hasKeyCallback,
                logger, String.format("Get %s from local", key));

        callbackBuilderForLocal.withCallback(Boolean.class, present -> {
            if (present) {
                hasKeyCallback.returnThis(true);
            } else {

                final CallbackBuilder callbackBuilderForRemote = reactor.callbackBuilder().delegateWithLogging(hasKeyCallback,
                        logger, String.format("Get %s from remote", key));
                callbackBuilderForRemote.withCallback(Boolean.class, hasKeyCallback::returnThis);
                remoteKeyValueStore.hasKey(callbackBuilderForRemote.build(), key);
            }
        });

        localKeyValueStore.hasKey(callbackBuilderForLocal.build(), key);

    }

    @Override
    public void delete(final String key) {

        localKeyValueStore.delete(key);
        remoteKeyValueStore.delete(key);
    }

    @Override
    public void deleteWithConfirmation(final Callback<Boolean> confirmation, final String key) {


        final CallbackBuilder callbackBuilder = getCallbackBuilderForPut(confirmation, key);
        localKeyValueStore.deleteWithConfirmation(callbackBuilder.build(), key);
        remoteKeyValueStore.deleteWithConfirmation(callbackBuilder.build(), key);

    }


    @QueueCallback({QueueCallbackType.EMPTY, QueueCallbackType.LIMIT, QueueCallbackType.IDLE})
    public void process() {
        reactor.process();
        remoteKeyValueStore.process();
        localKeyValueStore.process();
    }
}
