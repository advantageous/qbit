package io.advantageous.qbit.admin;


import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.reakt.Reakt;
import io.advantageous.qbit.service.health.ServiceHealthManager;
import io.advantageous.qbit.service.stats.StatsCollector;
import io.advantageous.qbit.util.Timer;
import io.advantageous.reakt.Expected;
import io.advantageous.reakt.promise.Promise;
import io.advantageous.reakt.reactor.Reactor;

import java.util.HashMap;
import java.util.function.Consumer;

/**
 * Common things that you need for QBit/Reakt services.
 * Gets rid of most of the boilerplate code.
 */
public class ServiceManagementBundle implements ServiceHealthManager {

    private final Reactor reactor;
    private final StatsCollector stats;
    private final ServiceHealthManager healthManager;
    private final String serviceName;
    private final Timer timer;
    private final String statKeyPrefix;
    private final HashMap<String, String> statNameMap;
    private final Expected<Runnable> processHandler;
    protected long time;

    public ServiceManagementBundle(final Reactor reactor,
                                   final StatsCollector stats,
                                   final ServiceHealthManager serviceHealthManager,
                                   final String serviceName,
                                   final Timer timer,
                                   final String statKeyPrefix,
                                   final Runnable processHandler) {
        this.reactor = reactor;
        this.stats = stats;
        this.healthManager = serviceHealthManager;
        this.serviceName = serviceName;
        this.timer = timer;
        this.statKeyPrefix = statKeyPrefix;
        this.statNameMap = new HashMap<>();
        this.processHandler = Expected.ofNullable(processHandler);
    }


    public void process() {
        time = timer.time();
        reactor.process();
        processHandler.ifPresent(Runnable::run);

    }

    /**
     * Creates a QBit callback based on promise created.
     * @param promiseConsumer promise consumer
     * @param <T> T
     * @return QBit callback
     */
    public <T> Callback<T> callback(final Consumer<Promise<T>> promiseConsumer) {
        Promise<T> promise = reactor.promise();
        promiseConsumer.accept(promise);
        return Reakt.convertPromise(promise);
    }

    /**
     * Prefixes the stats key with the stat key prefix, and then calls statsCollector.recordLevel.
     *
     * @param statKey statKey
     * @param level   level
     */
    public void recordLevel(final String statKey, final long level) {
        final String longKey = getActualStatKey(statKey);
        stats.recordLevel(longKey, level);
    }


    /**
     * Prefixes the stats key with the stat key prefix, and then calls statsCollector.recordCount.
     *
     * @param statKey statKey
     * @param count   count
     */
    public void recordCount(final String statKey, final long count) {
        final String longKey = getActualStatKey(statKey);
        stats.recordCount(longKey, count);
    }

    /**
     * Prefixes the stats key with the stat key prefix, and then calls statsCollector.recordCount.
     *
     * @param statKey statKey
     */
    public void increment(final String statKey) {

        final String longKey = getActualStatKey(statKey);
        stats.increment(longKey);
    }


    /**
     * Prefixes the stats key with the stat key prefix, and then calls statsCollector.recordTiming.
     *
     * @param statKey  statKey
     * @param timeSpan timeSpan
     */
    public void recordTiming(String statKey, long timeSpan) {
        final String longKey = getActualStatKey(statKey);
        stats.recordTiming(longKey, timeSpan);
    }


    private String getActualStatKey(String statKey) {
        String longKey = statNameMap.get(statKey);
        if (longKey == null) {
            longKey = this.statKeyPrefix + statKey;
        }
        return longKey;
    }

    public Reactor reactor() {
        return reactor;
    }

    public StatsCollector stats() {
        return stats;
    }

    public ServiceHealthManager health() {
        return healthManager;
    }

    public String getServiceName() {
        return serviceName;
    }

    public Timer getTimer() {
        return timer;
    }

    public long getTime() {
        return time;
    }

    @Override
    public boolean isFailing() {
        return healthManager.isFailing();
    }

    @Override
    public boolean isOk() {
        return healthManager.isOk();
    }

    @Override
    public void setFailing() {
        healthManager.setFailing();
    }

    @Override
    public void recover() {
        healthManager.recover();
    }
}
