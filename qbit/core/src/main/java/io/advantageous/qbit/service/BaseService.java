package io.advantageous.qbit.service;

import io.advantageous.boon.core.Str;
import io.advantageous.qbit.queue.QueueCallBackHandler;
import io.advantageous.qbit.reactive.Reactor;
import io.advantageous.qbit.service.stats.StatsCollector;
import io.advantageous.qbit.util.Timer;

import java.util.HashMap;

public abstract class BaseService implements QueueCallBackHandler{


    protected final StatsCollector statsCollector;
    protected final Reactor reactor;
    protected final Timer timer;
    private final String statKeyPrefix;
    protected long time;
    private final HashMap<String, String> statNameMap;

    public BaseService(Reactor reactor, Timer timer, final StatsCollector statsCollector) {
        this.statsCollector = statsCollector;
        this.reactor = reactor;
        this.timer = timer;
        statKeyPrefix = Str.underBarCase(this.getClass().getSimpleName()).replace('_', '.') + ".";
        statNameMap = new HashMap<>();
    }


    public BaseService(String statKeyPrefix, Reactor reactor, Timer timer, final StatsCollector statsCollector) {
        this.statsCollector = statsCollector;
        this.reactor = reactor;
        this.timer = timer;
        this.statKeyPrefix = statKeyPrefix;
        statNameMap = new HashMap<>();
    }


    /** Prefixes the stats key with the stat key prefix, and then calls statsCollector.recordLevel.
     *
     * @param statKey statKey
     * @param level level
     */
    protected void recordLevel(final String statKey, final long level) {
        final String longKey = getActualStatKey(statKey);
        statsCollector.recordLevel(longKey, level);
    }


    /** Prefixes the stats key with the stat key prefix, and then calls statsCollector.recordCount.
     *
     * @param statKey statKey
     * @param count count
     */
    protected void recordCount(final String statKey, final long count) {
        final String longKey = getActualStatKey(statKey);
        statsCollector.recordCount(longKey, count);
    }


    /** Prefixes the stats key with the stat key prefix, and then calls statsCollector.recordTiming.
     *
     * @param statKey statKey
     * @param timeSpan timeSpan
     */
    protected void recordTime(String statKey, long timeSpan) {
        final String longKey = getActualStatKey(statKey);
        statsCollector.recordTiming(longKey, timeSpan);
    }


    private String getActualStatKey(String statKey) {
        String longKey = statNameMap.get(statKey);
        if (longKey == null) {
            longKey = this.statKeyPrefix + statKey;
        }
        return longKey;
    }


    @Override
    public void queueLimit() {
        doProcess();
    }

    @Override
    public void queueEmpty() {
        doProcess();
    }

    @Override
    public void queueIdle() {
        doProcess();
    }


    private void doProcess() {
        time = timer.time();
        reactor.process();
        process();
    }

    protected  void process() {

    }

}
