package io.advantageous.qbit.kvstore.lowlevel;

import io.advantageous.boon.primitive.SimpleLRUCache;
import io.advantageous.qbit.annotation.QueueCallback;
import io.advantageous.qbit.annotation.QueueCallbackType;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.reactive.Reactor;
import io.advantageous.qbit.service.stats.StatsCollector;
import io.advantageous.qbit.time.Duration;
import io.advantageous.qbit.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static io.advantageous.qbit.time.Duration.FIVE_SECONDS;
import static io.advantageous.qbit.time.Duration.TEN_SECONDS;

/**
 * ***LowLevelLocalKeyValueStoreService*** (implements `LowLevelKeyValueStoreService`)
 * is a near cache (in memory) for byte arrays and strings.
 */
public class LowLevelLocalKeyValueStoreService implements LowLevelKeyValueStoreService {

    public final String BASE_STAT_KEY = "qbit.kv.store.";
    public final String CACHE_SIZE_AT_FLUSH = BASE_STAT_KEY + "flush.size";
    public final String CACHE_SIZE = BASE_STAT_KEY + "cache.size";
    private final int localCacheSize;
    private final Timer timer;
    private final Reactor reactor;
    private final StatsCollector statsCollector;
    private final Logger logger = LoggerFactory.getLogger(LowLevelLocalKeyValueStoreService.class);
    private SimpleLRUCache<String, CacheEntry> localCache;
    private long time;


    public LowLevelLocalKeyValueStoreService(final Timer timer,
                                             final Reactor reactor,
                                             final int localCacheSize,
                                             final StatsCollector statsCollector,
                                             final Optional<Duration> flushCacheDuration,
                                             final boolean debug) {
        this.localCacheSize = localCacheSize;
        this.timer = timer;
        this.reactor = reactor;
        this.statsCollector = statsCollector;

        reactor.addServiceToFlush(statsCollector);

        if (flushCacheDuration.isPresent()) {
            reactor.addRepeatingTask(flushCacheDuration.get(), this::localCacheInit);
        }

        if (debug || logger.isDebugEnabled()) {
            reactor.addRepeatingTask(TEN_SECONDS, this::debug);
        }

        reactor.addRepeatingTask(FIVE_SECONDS,
                () -> statsCollector.recordLevel(CACHE_SIZE, localCache.size())
        );

        localCacheInit();


    }

    private void debug() {

        logger.info("DEBUG ############");
        logger.info("LOCAL CACHE KEYS {}", localCache.keys());
        logger.info("LOCAL CACHE VALUES {}", localCache.values());

        logger.info("DEBUG ############");
    }

    @QueueCallback({QueueCallbackType.EMPTY, QueueCallbackType.LIMIT})
    public void process() {
        reactor.process();
        time = timer.time();
    }

    private void localCacheInit() {
        if (localCache != null) {
            statsCollector.recordLevel(CACHE_SIZE_AT_FLUSH, this.localCache.size());
        }
        localCache = new SimpleLRUCache<>(this.localCacheSize);
    }

    @Override
    public void deleteWithConfirmation(final Callback<Boolean> confirmation, final String key) {
        localCache.remove(key);
        confirmation.accept(true);
    }

    @Override
    public void putString(final String key, final String value) {
        localCache.put(key, new CacheStringEntry(key, Optional.empty(), 0L, value));
    }

    @Override
    public void putBytes(String key, byte[] value) {
        localCache.put(key, new CacheBytesEntry(key, Optional.empty(), 0L, value));
    }

    @Override
    public void putStringWithConfirmation(final Callback<Boolean> confirmation,
                                          final String key,
                                          final String value) {
        localCache.put(key, new CacheStringEntry(key, Optional.empty(), 0L, value));
        confirmation.returnThis(true);
    }

    @Override
    public void putBytesWithConfirmation(final Callback<Boolean> confirmation,
                                         final String key,
                                         final byte[] value) {
        localCache.put(key, new CacheBytesEntry(key, Optional.empty(), 0L, value));
        confirmation.returnThis(true);
    }

    @Override
    public void putStringWithConfirmationAndTimeout(final Callback<Boolean> confirmation,
                                                    final String key,
                                                    final String value,
                                                    final Duration expiry) {
        localCache.put(key, new CacheStringEntry(key, Optional.of(expiry), time, value));
        confirmation.returnThis(true);
    }

    @Override
    public void putBytesWithConfirmationAndTimeout(Callback<Boolean> confirmation, String key, byte[] value, Duration expiry) {
        localCache.put(key, new CacheBytesEntry(key, Optional.of(expiry), time, value));
        confirmation.returnThis(true);
    }

    @Override
    public void putStringWithTimeout(final String key,
                                     final String value,
                                     final Duration expiry) {
        localCache.put(key, new CacheStringEntry(key, Optional.of(expiry), time, value));
    }

    @Override
    public void putBytesWithTimeout(final String key,
                                    final byte[] value,
                                    final Duration expiry) {
        localCache.put(key, new CacheBytesEntry(key, Optional.of(expiry), time, value));
    }

    @Override
    public void getString(final Callback<Optional<String>> callback,
                          final String key) {

        final CacheEntry cacheEntry = localCache.get(key);
        if (cacheEntry == null) {
            callback.returnThis(Optional.<String>empty());
            return;
        }
        if (cacheEntry.isExpired(time)) {
            localCache.remove(key);
            callback.returnThis(Optional.<String>empty());
        } else {
            final String value = ((CacheStringEntry) cacheEntry).value;
            if (value == null) {
                callback.returnThis(Optional.<String>empty());
            } else {
                callback.returnThis(Optional.of(value));
            }
        }
    }

    @Override
    public void getBytes(final Callback<Optional<byte[]>> callback,
                         final String key) {

        final CacheEntry cacheEntry = localCache.get(key);
        if (cacheEntry == null) {
            callback.returnThis(Optional.<byte[]>empty());
            return;
        }
        if (cacheEntry.isExpired(time)) {
            localCache.remove(key);
            callback.returnThis(Optional.<byte[]>empty());
        } else {
            final byte[] value = ((CacheBytesEntry) cacheEntry).value;
            if (value == null) {
                callback.returnThis(Optional.<byte[]>empty());
            } else {
                callback.returnThis(Optional.of(value));
            }
        }
    }

    @Override
    public void hasKey(final Callback<Boolean> hasKeyCallback,
                       final String key) {

        final CacheEntry cacheEntry = localCache.getSilent(key);
        if (cacheEntry == null) {
            hasKeyCallback.returnThis(false);
        } else {
            if (cacheEntry.isExpired(time)) {
                localCache.remove(key);
                hasKeyCallback.returnThis(false);
            } else {
                final Object value = cacheEntry.getValue();
                if (value == null) {
                    hasKeyCallback.returnThis(false);
                } else {
                    hasKeyCallback.returnThis(true);
                }
            }
        }
    }

    @Override
    public void delete(final String key) {
        localCache.remove(key);
    }

    private abstract static class CacheEntry {
        private final Optional<Duration> expiry;
        private final String key;
        private final long createTime;

        public CacheEntry(Optional<Duration> expiry, String key, long createTime) {
            this.expiry = expiry;
            this.key = key;
            this.createTime = createTime;
        }

        abstract Object getValue();

        private boolean isExpired(long currentTime) {
            if (!expiry.isPresent()) {
                return false;
            }
            long duration = currentTime - createTime;
            return duration > expiry.get().toMillis();
        }

    }

    private class CacheBytesEntry extends CacheEntry {
        private final byte[] value;

        private CacheBytesEntry(String key, Optional<Duration> expiry, long createTime, byte[] value) {
            super(expiry, key, createTime);
            this.value = value;
        }


        Object getValue() {
            return value;
        }
    }

    private class CacheStringEntry extends CacheEntry {
        private final String value;

        private CacheStringEntry(String key, Optional<Duration> expiry, long createTime, String value) {
            super(expiry, key, createTime);
            this.value = value;
        }


        Object getValue() {
            return value;
        }

    }

}
