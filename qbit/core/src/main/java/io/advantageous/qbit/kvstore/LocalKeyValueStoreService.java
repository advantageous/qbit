package io.advantageous.qbit.kvstore;

import io.advantageous.boon.primitive.SimpleLRUCache;
import io.advantageous.qbit.annotation.QueueCallback;
import io.advantageous.qbit.annotation.QueueCallbackType;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.reactive.CallbackBuilder;
import io.advantageous.qbit.reactive.Reactor;
import io.advantageous.qbit.service.stats.StatsCollector;
import io.advantageous.qbit.time.Duration;
import io.advantageous.qbit.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;


public class LocalKeyValueStoreService<T> implements KeyValueStoreService<T> {

    private final StatsCollector statsCollector;
    private final String statKey;
    private final FallbackReader<T> fallbackReader;
    private final WriteBehindWriter<T> writeBehindWriter;
    private final Reactor reactor;
    private final Logger logger = LoggerFactory.getLogger(LocalKeyValueStoreService.class);
    private final boolean debug;
    private final Timer timer;
    private long time = 0;
    private int cacheSize = 0;
    private SimpleLRUCache<String, CacheEntry<T>> cache;

    public LocalKeyValueStoreService(
            final Reactor reactor,
            final Timer timer,
            final FallbackReader<T> fallbackReader,
            final WriteBehindWriter<T> writeBehindWriter,
            final int cacheSize,
            final Duration flushEvery,
            final StatsCollector statsCollector,
            final String statKey,
            final Duration debugInterval,
            final boolean debug) {


        this.fallbackReader = fallbackReader;
        this.writeBehindWriter = writeBehindWriter;
        this.cacheSize = cacheSize;
        this.reactor = reactor;
        this.reactor.addRepeatingTask(flushEvery, this::initCache);
        this.statsCollector = statsCollector;
        this.timer = timer;
        this.statKey = statKey;

        if (debugInterval != Duration.NEVER) {
            this.reactor.addRepeatingTask(flushEvery, this::debugCache);
        }

        this.debug = debug || logger.isDebugEnabled();
        initCache();
    }

    private void debugCache() {
        cache.keys().forEach(s -> {
            logger.info("CACHE ENTRY {}", cache.getSilent(s));
        });
    }

    private void initCache() {
        logger.info("flushing cache");
        cache = new SimpleLRUCache<>(this.cacheSize);
    }

    private CacheEntry<T> cacheEntry(final String key, final T value) {
        return new CacheEntry<>(key, value, time, Optional.empty());
    }


    private CacheEntry<T> cacheEntryWithExpiry(final String key,
                                               final T value,
                                               final Duration duration) {
        return new CacheEntry<>(key, value, time, Optional.of(duration));
    }

    @Override
    public void put(final String key,
                    final T value) {
        cache.put(key, cacheEntry(key, value));
        writeBehindWriter.write(key, value);
    }

    @Override
    public void putWithConfirmation(final Callback<Boolean> confirmation,
                                    final String key,
                                    final T value) {

        /* Write it local. */
        cache.put(key, cacheEntry(key, value));


        final CallbackBuilder callbackBuilder = reactor.callbackBuilder();
        if (debug) {
            callbackBuilder.wrapWithLogging(confirmation, logger,
                    String.format("put with confirmation %s", key));
        } else {
            callbackBuilder.wrapWithLogging(confirmation, logger, "put with confirmation");
        }
        writeBehindWriter.writeWithConfirmation(callbackBuilder.build(), key, value);
    }

    @Override
    public void putWithConfirmationAndTimeout(final Callback<Boolean> confirmation,
                                              final String key,
                                              final T value,
                                              final Duration expiry) {


        cache.put(key, cacheEntryWithExpiry(key, value, expiry));


        final CallbackBuilder callbackBuilder = reactor.callbackBuilder();


        if (debug) {
            callbackBuilder.wrapWithLogging(confirmation, logger,
                    String.format("put with confirmation %s and timeout %s", key, expiry));
        } else {
            callbackBuilder.wrapWithLogging(confirmation, logger, "put with confirmation and timeout");
        }
        writeBehindWriter.writeWithConfirmationAndTimeout(callbackBuilder.build(), key, value, expiry);
    }

    @Override
    public void putWithTimeout(final String key,
                               final T value,
                               final Duration expiry) {
        cache.put(key, cacheEntryWithExpiry(key, value, expiry));
        writeBehindWriter.writeWithTimeout(key, value, expiry);
    }

    @Override
    public void get(final Callback<Optional<T>> callback,
                    final String key) {

        final CacheEntry<T> cacheEntry = doGetCacheEntry(callback, key);

        if (cacheEntry == null) {
            final CallbackBuilder callbackBuilder = reactor.callbackBuilder();
            if (debug) {
                callbackBuilder.wrapWithLogging(callback, logger,
                        String.format("get  %s", key));
            } else {
                callbackBuilder.wrapWithLogging(callback, logger, "get operation");
            }
            fallbackReader.get(callbackBuilder.build(), key);
        }
    }

    private CacheEntry<T> doGetCacheEntry(final Callback<Optional<T>> callback,
                                          final String key) {
        CacheEntry<T> cacheEntry = cache.get(key);
        if (cacheEntry != null) {
            if (cacheEntry.isExpired(time)) {
                statsCollector.increment(statKey + "expire");
                cache.remove(key); //do not return so we can look things up in the fallbackReader.
                return null;
            } else {
                final T value = cacheEntry.getValue();
                if (value == null) {
                    callback.returnThis(Optional.<T>empty());
                } else {
                    statsCollector.increment(statKey + "cacheHit");
                    callback.returnThis(Optional.of(value));
                }
            }
        }
        return cacheEntry;
    }

    @Override
    public void hasKey(final Callback<Boolean> hasKeyCallback, final String key) {

        final CacheEntry<T> cacheEntry = cache.getSilent(key);
        if (cacheEntry != null) {
            if (cacheEntry.isExpired(time)) {
                statsCollector.increment(statKey + "expire");
                cache.remove(key); //do not return so we can look things up in the fallbackReader.
            } else {
                final T value = cacheEntry.getValue();
                if (value == null) {
                    hasKeyCallback.returnThis(false);
                } else {
                    hasKeyCallback.returnThis(true);
                }
                return;
            }
        }

        final CallbackBuilder callbackBuilder = reactor.callbackBuilder();
        if (debug) {
            callbackBuilder.wrapWithLogging(hasKeyCallback, logger,
                    String.format("get  %s", key));
        } else {
            callbackBuilder.wrapWithLogging(hasKeyCallback, logger, "hasKey operation");
        }
        fallbackReader.hasKey(callbackBuilder.build(), key);

    }

    @Override
    public void delete(final String key) {

        cache.remove(key);
        writeBehindWriter.delete(key);

    }

    @Override
    public void deleteWithConfirmation(Callback<Boolean> confirmation, String key) {

        cache.remove(key);

        final CallbackBuilder callbackBuilder = reactor.callbackBuilder();
        if (debug) {
            callbackBuilder.wrapWithLogging(confirmation, logger,
                    String.format("get  %s", key));
        } else {
            callbackBuilder.wrapWithLogging(confirmation, logger, "hasKey operation");
        }
        writeBehindWriter.deleteWithConfirmation(callbackBuilder.build(), key);
    }

    @Override
    public void wipeCache() {
        initCache();
    }

    @QueueCallback({QueueCallbackType.EMPTY, QueueCallbackType.LIMIT, QueueCallbackType.IDLE})
    public void process() {
        reactor.process();
        time = timer.time();
        fallbackReader.flushRequests();
        writeBehindWriter.flushRequests();
    }

    private class CacheEntry<V> {


        private final V value;
        private final Optional<Duration> expiry;
        private final String key;
        private final long createTime;

        public CacheEntry(final String key,
                          final V value,
                          final long createTime,
                          final Optional<Duration> expiry) {
            statsCollector.increment(statKey + "cacheEntryAdded");
            this.value = value;
            this.expiry = expiry;
            this.key = key;
            this.createTime = createTime;
        }

        private boolean isExpired(long currentTime) {
            if (!expiry.isPresent()) {
                return false;
            }
            long duration = currentTime - createTime;
            return duration > expiry.get().toMillis();
        }

        public V getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "CacheEntry{" +
                    "value=" + value +
                    ", expiry=" + expiry +
                    ", key='" + key + '\'' +
                    ", createTime=" + createTime +
                    '}';
        }
    }
}
