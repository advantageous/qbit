package io.advantageous.qbit.service.stats;

import io.advantageous.qbit.service.ServiceProxyUtils;

import java.util.HashMap;
import java.util.Map;


/**
 * This is a drop in replacement for a StatsCollector proxy,
 * but it provides buffering of stats.
 */
public class StatsCollectorBuffer implements StatsCollector {

    private final Map<String, RecordCount> counts = new HashMap<>();
    private final Map<String, RecordLevel> levels = new HashMap<>();
    private final Map<String, RecordTiming> timings = new HashMap<>();
    private final StatsCollector statsCollector;

    public StatsCollectorBuffer(StatsCollector statsCollector) {
        this.statsCollector = statsCollector;
    }

    static class RecordTiming {
        final StatList list = new StatList();
        final String name;

        RecordTiming(String name) {
            this.name = name;
        }

        void set(long timing) {
            list.addLong(timing);
        }
    }

    static class RecordCount {
        long count;
        final String name;

        RecordCount(String name) {
            this.name = name;
        }

        void set(long count) {
            this.count += count;
        }
    }


    static class RecordLevel {
        long level;
        long lastLevelSent;

        final String name;
        RecordLevel(String name) {
            this.name = name;
        }
        void set(long level) {
            this.level = level;
        }
        void setLastLevelSent(long lastLevelSent) {
            this.lastLevelSent = lastLevelSent;
        }
    }


    @Override
    public void increment(final String name) {
        recordCount(name, 1L);
    }

    @Override
    public void recordCount(final String name, final long count) {
        RecordCount recordCount = counts.get(name);
        if (recordCount == null) {
            recordCount = new RecordCount(name);
            counts.put(name, recordCount);
        }
        recordCount.set(count);
    }

    @Override
    public void recordLevel(final String name, final long level) {
        RecordLevel recordLevel = levels.get(name);
        if (recordLevel == null) {
            recordLevel = new RecordLevel(name);
            levels.put(name, recordLevel);
        }
        recordLevel.set(level);
    }

    @Override
    public void recordTiming(final String name, long timing) {
        RecordTiming recordTiming = timings.get(name);
        if (recordTiming == null) {
            recordTiming = new RecordTiming(name);
            timings.put(name, recordTiming);
        }
        recordTiming.set(timing);
    }

    public void sendStats() {
        counts.values().forEach(recordCount -> {
            if (recordCount.count > 0) {
                statsCollector.recordCount(recordCount.name, recordCount.count);
                recordCount.count = 0;
            }
        });

        levels.values().forEach(recordLevel -> {
            if (recordLevel.level != recordLevel.lastLevelSent) {
                recordLevel.setLastLevelSent(recordLevel.level);
                statsCollector.recordLevel(recordLevel.name, recordLevel.level);
            }
        });

        timings.values().forEach(recordTiming -> {
            if (recordTiming.list.size() == 0) {
                return;
            }
            final long max = recordTiming.list.max();
            if (max > 0) {
                statsCollector.recordTiming(recordTiming.name, max);
            }
            recordTiming.list.clear();
        });

        ServiceProxyUtils.flushServiceProxy(statsCollector);

    }

    @Override
    public void clientProxyFlush() {
        sendStats();
    }
}
