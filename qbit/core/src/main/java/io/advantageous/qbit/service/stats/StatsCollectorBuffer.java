/**
 * ****************************************************************************
 * <p>
 * Copyright (c) 2015. Rick Hightower, Geoff Chandler
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
 * http://rick-hightower.blogspot.com/2014/12/rise-of-machines-writing-high-speed.html
 * http://rick-hightower.blogspot.com/2014/12/quick-guide-to-programming-services-in.html
 * http://rick-hightower.blogspot.com/2015/01/quick-startClient-qbit-programming.html
 * http://rick-hightower.blogspot.com/2015/01/high-speed-soa.html
 * http://rick-hightower.blogspot.com/2015/02/qbit-event-bus.html
 * <p>
 * ****************************************************************************
 */
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
        final String name;
        long count;

        RecordCount(String name) {
            this.name = name;
        }

        void set(long count) {
            this.count += count;
        }
    }

    static class RecordLevel {
        final String name;
        long level;
        long lastLevelSent;

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
}
