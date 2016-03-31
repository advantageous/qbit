package io.advantageous.qbit.kvstore;

import io.advantageous.qbit.client.ClientProxy;
import io.advantageous.qbit.kvstore.impl.StringDecoderEncoderKeyValueStore;
import io.advantageous.qbit.kvstore.lowlevel.LowLevelKeyValueStoreService;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.reactive.Reactor;
import io.advantageous.qbit.reactive.ReactorBuilder;
import io.advantageous.qbit.service.ServiceBuilder;
import io.advantageous.qbit.service.ServiceProxyUtils;
import io.advantageous.qbit.service.ServiceQueue;
import io.advantageous.qbit.service.stats.StatsCollector;
import io.advantageous.qbit.time.Duration;
import io.advantageous.qbit.util.Timer;

import java.util.Optional;

public class LocalKeyValueStoreServiceBuilder<T> {

    private boolean debug;
    private Reactor reactor;
    private Timer timer;
    private Class<T> classType;
    private FallbackReader<T> fallbackReader;
    private WriteBehindWriter<T> writeBehindWriter;
    private int cacheSize = 10_000;
    private Duration flushEvery = Duration.ONE_HOUR;
    private StatsCollector statsCollector;
    private String statKey = "qbit.kv.object.store.";
    private Duration debugInterval = Duration.NEVER;
    private ServiceBuilder serviceBuilder;

    public static <T> LocalKeyValueStoreServiceBuilder<T> localKeyValueStoreServiceBuilder(final Class<T> classType) {
        LocalKeyValueStoreServiceBuilder<T> builder = new LocalKeyValueStoreServiceBuilder<>();
        builder.setClassType(classType);
        return builder;
    }

    public LocalKeyValueStoreServiceBuilder<T> setWriteBehindAndReadFallbackAsLowLevel(final LowLevelKeyValueStoreService lowLevelKVStore) {

        final StringDecoderEncoderKeyValueStore<T> keyValueStore = JsonKeyValueStoreServiceBuilder.jsonKeyValueStoreServiceBuilder()
                .setLowLevelKeyValueStoreService(lowLevelKVStore).buildKeyValueStore(getClassType());

        setWriteBehindAndReadFallback(keyValueStore);
        return this;

    }

    public LocalKeyValueStoreServiceBuilder<T> setWriteBehindAndReadFallback(
            final KeyValueStoreService<T> keyValueStoreServiceInternal) {

        final KeyValueStoreService<T> keyValueStoreService =
                keyValueStoreServiceInternal instanceof ClientProxy ? keyValueStoreServiceInternal
                        : getServiceBuilder().setServiceObject(keyValueStoreServiceInternal).buildAndStartAll()
                        .createProxy(KeyValueStoreService.class);

        setWriteBehind(keyValueStoreService);
        setReadFallback(keyValueStoreService);

        return this;
    }

    public LocalKeyValueStoreServiceBuilder<T> setWriteBehind(final KeyValueStoreService<T> keyValueStoreServiceInternal) {

        final KeyValueStoreService<T> keyValueStoreService =
                keyValueStoreServiceInternal instanceof ClientProxy ? keyValueStoreServiceInternal
                        : getServiceBuilder().setServiceObject(keyValueStoreServiceInternal).buildAndStartAll()
                        .createProxy(KeyValueStoreService.class);

        setWriteBehindWriter(new WriteBehindWriter<T>() {
            @Override
            public void writeWithConfirmation(Callback<Boolean> confirmation, String key, T value) {
                keyValueStoreService.putWithConfirmation(confirmation, key, value);
            }

            @Override
            public void write(String key, T value) {
                keyValueStoreService.put(key, value);

            }

            @Override
            public void writeWithConfirmationAndTimeout(Callback<Boolean> confirmation, String key, T value, Duration expiry) {
                keyValueStoreService.putWithConfirmationAndTimeout(confirmation, key, value, expiry);
            }

            @Override
            public void writeWithTimeout(String key, T value, Duration expiry) {
                keyValueStoreService.putWithTimeout(key, value, expiry);

            }

            @Override
            public void delete(String key) {
                keyValueStoreService.delete(key);
            }

            @Override
            public void deleteWithConfirmation(Callback<Boolean> confirmation, String key) {
                keyValueStoreService.deleteWithConfirmation(confirmation, key);
            }

            @Override
            public void flushRequests() {

                ServiceProxyUtils.flushServiceProxy(keyValueStoreService);
            }
        });

        return this;
    }

    public LocalKeyValueStoreServiceBuilder<T> setReadFallback(final KeyValueStoreService<T> keyValueStoreServiceInternal) {

        final KeyValueStoreService<T> keyValueStoreService =
                keyValueStoreServiceInternal instanceof ClientProxy ? keyValueStoreServiceInternal
                        : getServiceBuilder().setServiceObject(keyValueStoreServiceInternal).buildAndStartAll()
                        .createProxy(KeyValueStoreService.class);

        setFallbackReader(new FallbackReader<T>() {

            @Override
            public void get(Callback<Optional<T>> callback, String key) {
                keyValueStoreService.get(callback, key);
            }

            @Override
            public void hasKey(Callback<Boolean> callback, String key) {
                keyValueStoreService.hasKey(callback, key);
            }

            @Override
            public void flushRequests() {
                ServiceProxyUtils.flushServiceProxy(keyValueStoreService);
            }
        });

        return this;
    }

    public ServiceBuilder getServiceBuilder() {
        if (serviceBuilder == null) {
            serviceBuilder = new ServiceBuilder();
            return serviceBuilder;
        }
        return serviceBuilder.copy();
    }

    public LocalKeyValueStoreServiceBuilder setServiceBuilder(ServiceBuilder serviceBuilder) {
        this.serviceBuilder = serviceBuilder;
        return this;
    }

    public Reactor getReactor() {
        if (reactor == null) {
            reactor = ReactorBuilder.reactorBuilder().build();
        }
        return reactor;
    }

    public LocalKeyValueStoreServiceBuilder setReactor(Reactor reactor) {
        this.reactor = reactor;
        return this;
    }

    public Timer getTimer() {
        if (timer == null) {
            timer = Timer.timer();
        }
        return timer;
    }

    public LocalKeyValueStoreServiceBuilder setTimer(Timer timer) {
        this.timer = timer;
        return this;
    }

    public Class<T> getClassType() {
        return classType;
    }

    public LocalKeyValueStoreServiceBuilder setClassType(Class<T> classType) {
        this.classType = classType;
        return this;
    }

    public FallbackReader<T> getFallbackReader() {

        if (fallbackReader == null) {
            fallbackReader = new FallbackReader<T>() {
            };
        }
        return fallbackReader;
    }

    public LocalKeyValueStoreServiceBuilder setFallbackReader(FallbackReader<T> fallbackReader) {
        this.fallbackReader = fallbackReader;
        return this;
    }

    public WriteBehindWriter<T> getWriteBehindWriter() {
        if (writeBehindWriter == null) {
            writeBehindWriter = new WriteBehindWriter<T>() {
            };
        }
        return writeBehindWriter;
    }

    public LocalKeyValueStoreServiceBuilder setWriteBehindWriter(WriteBehindWriter<T> writeBehindWriter) {

        this.writeBehindWriter = writeBehindWriter;
        return this;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public LocalKeyValueStoreServiceBuilder setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
        return this;
    }

    public Duration getFlushEvery() {
        return flushEvery;
    }

    public LocalKeyValueStoreServiceBuilder setFlushEvery(Duration flushEvery) {
        this.flushEvery = flushEvery;
        return this;
    }

    public StatsCollector getStatsCollector() {
        if (statsCollector == null) {
            statsCollector = new StatsCollector() {
            };
        }
        return statsCollector;
    }

    public LocalKeyValueStoreServiceBuilder setStatsCollector(StatsCollector statsCollector) {
        this.statsCollector = statsCollector;
        return this;
    }

    public String getStatKey() {
        return statKey;
    }

    public LocalKeyValueStoreServiceBuilder setStatKey(String statKey) {
        this.statKey = statKey;
        return this;
    }

    public Duration getDebugInterval() {
        return debugInterval;
    }

    public LocalKeyValueStoreServiceBuilder setDebugInterval(Duration debugInterval) {
        this.debugInterval = debugInterval;
        return this;
    }

    public LocalKeyValueStoreService<T> build() {

        return new LocalKeyValueStoreService<>(
                getReactor(),
                getTimer(),
                getFallbackReader(),
                getWriteBehindWriter(),
                getCacheSize(),
                getFlushEvery(),
                getStatsCollector(),
                getStatKey(),
                getDebugInterval(),
                getDebug());
    }


    public ServiceQueue buildAsService() {
        final LocalKeyValueStoreService<T> kvStoreInternal = build();
        return getServiceBuilder().setServiceObject(kvStoreInternal).build();
    }


    public ServiceQueue buildAsServiceAndStartAll() {
        final ServiceQueue serviceQueue = buildAsService();
        serviceQueue.startAll();
        return serviceQueue;
    }

    public boolean getDebug() {
        return debug;
    }

    public LocalKeyValueStoreServiceBuilder setDebug(boolean debug) {

        this.debug = debug;
        return this;
    }
}
