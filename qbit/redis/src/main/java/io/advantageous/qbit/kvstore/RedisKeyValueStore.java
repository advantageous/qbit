package io.advantageous.qbit.kvstore;

import io.advantageous.boon.core.Str;
import io.advantageous.qbit.kvstore.lowlevel.LowLevelKeyValueStoreService;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.time.Duration;
import io.vertx.redis.RedisClient;
import io.vertx.redis.op.SetOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class RedisKeyValueStore implements LowLevelKeyValueStoreService {

    private final RedisClient redisClient;
    private final Logger logger = LoggerFactory.getLogger(RedisKeyValueStore.class);

    public RedisKeyValueStore(final RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    @Override
    public void putString(final String key, final String value) {

        redisClient.set(key, value, event -> {
            if (event.failed()) {
                logger.error(String.format("Error calling put string %s", key),
                        event.cause());
            }
        });
    }

    @Override
    public void putStringWithConfirmation(final Callback<Boolean> confirmation,
                                          final String key,
                                          final String value) {
        redisClient.set(key, value, event -> {
            if (event.failed()) {
                confirmation.onError(event.cause());
            } else {
                confirmation.accept(true);
            }
        });
    }

    @Override
    public void putStringWithConfirmationAndTimeout(final Callback<Boolean> confirmation,
                                                    final String key,
                                                    final String value,
                                                    final Duration expiry) {
        final SetOptions setOptions = new SetOptions();
        setOptions.setPX(expiry.toMillis());
        redisClient.setWithOptions(key, value, setOptions, event -> {
            if (event.failed()) {
                confirmation.onError(event.cause());
            } else {
                confirmation.accept(true);
            }
        });
    }

    @Override
    public void putStringWithTimeout(final String key,
                                     final String value,
                                     final Duration expiry) {
        final SetOptions setOptions = new SetOptions();
        setOptions.setPX(expiry.toMillis());
        redisClient.setWithOptions(key, value, setOptions, event -> {
            if (event.failed()) {
                logger.error(String.format("Error calling put string %s", key),
                        event.cause());
            }
        });
    }

    @Override
    public void getString(final Callback<Optional<String>> callback,
                          final String key) {
        redisClient.get(key, event -> {
            if (event.failed()) {
                callback.onError(event.cause());
            } else {
                if (Str.isEmpty(event.result())) {
                    callback.returnThis(Optional.<String>empty());
                } else {
                    callback.returnThis(Optional.of(event.result()));
                }
            }
        });
    }

    @Override
    public void putBytes(final String key, final byte[] value) {
        /* This redis client does not support this.
         * https://github.com/vert-x3/vertx-redis-client/issues/41
         * https://github.com/vert-x3/vertx-redis-client/issues/40
         **/
        redisClient.setBinary(key, new String(value), event -> {

            if (event.failed()) {
                logger.error(String.format("Error calling put bytes %s", key),
                        event.cause());
            }
        });
    }

    @Override
    public void putBytesWithConfirmation(final Callback<Boolean> confirmation,
                                         final String key,
                                         final byte[] value) {
        /* This redis client does not support this.
         * https://github.com/vert-x3/vertx-redis-client/issues/41
         * https://github.com/vert-x3/vertx-redis-client/issues/40
         **/
        redisClient.setBinary(key, new String(value), event -> {
            if (event.failed()) {
                confirmation.onError(event.cause());
            } else {
                confirmation.accept(true);
            }
        });
    }

    @Override
    public void putBytesWithConfirmationAndTimeout(final Callback<Boolean> confirmation,
                                                   final String key,
                                                   final byte[] value,
                                                   final Duration expiry) {
        final SetOptions setOptions = new SetOptions();
        setOptions.setPX(expiry.toMillis());
        /* This redis client does not support this.
         * https://github.com/vert-x3/vertx-redis-client/issues/41
         * https://github.com/vert-x3/vertx-redis-client/issues/40
         **/
        redisClient.setWithOptions(key, new String(value), setOptions, event -> {
            if (event.failed()) {
                confirmation.onError(event.cause());
            } else {
                confirmation.accept(true);
            }
        });
    }

    @Override
    public void putBytesWithTimeout(final String key, final byte[] value,
                                    final Duration expiry) {

        final SetOptions setOptions = new SetOptions();
        setOptions.setPX(expiry.toMillis());
        /* This redis client does not support this.
         * https://github.com/vert-x3/vertx-redis-client/issues/41
         * https://github.com/vert-x3/vertx-redis-client/issues/40
         **/
        redisClient.setWithOptions(key, new String(value), setOptions, event -> {
            if (event.failed()) {

                logger.error(String.format("Error calling put bytes %s", key),
                        event.cause());
            }
        });

    }

    @Override
    public void getBytes(Callback<Optional<byte[]>> callback, String key) {
        /* This redis client does not support this.
         * https://github.com/vert-x3/vertx-redis-client/issues/41
         * https://github.com/vert-x3/vertx-redis-client/issues/40
         **/
        redisClient.getBinary(key, event -> {
            if (event.failed()) {
                callback.onError(event.cause());
            } else {
                if (Str.isEmpty(event.result())) {
                    callback.returnThis(Optional.<byte[]>empty());
                } else {
                    callback.returnThis(Optional.of(event.result().getBytes()));
                }
            }
        });
    }

    @Override
    public void hasKey(final Callback<Boolean> hasKeyCallback,
                       final String key) {
        redisClient.exists(key, event -> {
            if (event.failed()) {
                hasKeyCallback.onError(event.cause());
            } else {
                hasKeyCallback.returnThis(event.result() == 1);
            }
        });
    }

    @Override
    public void delete(final String key) {
        redisClient.del(key, event -> {
            if (event.failed()) {
                logger.error(String.format("Error calling put bytes %s", key),
                        event.cause());
            }
        });
    }

    @Override
    public void deleteWithConfirmation(final Callback<Boolean> confirmation,
                                       final String key) {
        redisClient.del(key, event -> {
            if (event.failed()) {
                confirmation.onError(event.cause());
            } else {
                confirmation.returnThis(event.result() > 0);
            }
        });
    }

    @Override
    public void process() {
        //No op
    }
}
