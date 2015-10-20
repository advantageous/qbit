package io.advantageous.qbit.kvstore;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;

public class RedisKeyValueStoreBuilder {

    private RedisOptions redisOptions;
    private Vertx vertx;
    private RedisClient redisClient;
    private VertxOptions vertxOptions;

    public VertxOptions getVertxOptions() {
        if (vertxOptions==null) {
            vertxOptions = new VertxOptions();
        }
        return vertxOptions;
    }

    public RedisKeyValueStoreBuilder setVertxOptions(VertxOptions vertxOptions) {
        this.vertxOptions = vertxOptions;
        return this;
    }

    public RedisClient getRedisClient() {

        if (redisClient == null) {
            redisClient = RedisClient.create(getVertx(), getRedisOptions());
        }
        return redisClient;
    }

    public RedisKeyValueStoreBuilder setRedisClient(RedisClient redisClient) {
        this.redisClient = redisClient;
        return this;
    }


    public RedisOptions getRedisOptions() {
        if (redisOptions==null) {
            redisOptions = new RedisOptions();
        }
        return redisOptions;
    }

    public RedisKeyValueStoreBuilder setRedisOptions(RedisOptions redisOptions) {
        this.redisOptions = redisOptions;
        return this;
    }

    public Vertx getVertx() {
        if (vertx == null) {
            vertx = Vertx.vertx(getVertxOptions());
        }
        return vertx;
    }

    public RedisKeyValueStoreBuilder setVertx(Vertx vertx) {
        this.vertx = vertx;
        return this;
    }


    public RedisKeyValueStore build() {
        return new RedisKeyValueStore(getRedisClient());
    }

    public static RedisKeyValueStoreBuilder redisKeyValueStoreBuilder() {
        return new RedisKeyValueStoreBuilder();
    }
}
